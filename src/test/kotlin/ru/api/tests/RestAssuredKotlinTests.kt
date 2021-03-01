package ru.api.tests

import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.apache.http.HttpStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode

class RestAssuredKotlinTests : RestAssuredBaseClass() {

    @Nested
    inner class CheckRetrievingBlogPosts {
        @Test
        internal fun `When the first blog post is requested then response should contains exactly that post`() {
            val responseBody =
                Given {
                    spec(requestSpecification)
                } When {
                    get("${BLOG_POSTS_ENDPOINT}/1")
                } Then {
                    statusCode(HttpStatus.SC_OK)
                } Extract {
                    body().asString()
                }

            val expectedResponse = """
                {
                  "userId": 1,
                  "id": 1,
                  "title": "sunt aut facere repellat provident occaecati excepturi optio reprehenderit",
                  "body": "quia et suscipit\nsuscipit recusandae consequuntur expedita et cum\nreprehenderit molestiae ut ut quas totam\nnostrum rerum est autem sunt rem eveniet architecto"
                }
            """.trimIndent()

            JSONAssert.assertEquals(expectedResponse, responseBody, JSONCompareMode.STRICT)
        }

        @Test
        internal fun `When request blog posts with query params then the response contains only requested post`() {
            val expectedResponse = BlogPost(userId = 1, id = 1, title = "123", body = "456")

            val response =
                Given {
                    spec(requestSpecification)
                    queryParam("id", expectedResponse.id)
                    queryParam("userId", expectedResponse.userId)
                } When {
                    get(BLOG_POSTS_ENDPOINT)
                } Then {
                    statusCode(HttpStatus.SC_OK)
                } Extract {
                    jsonPath().getList("", BlogPost::class.java)
                }

            assertThat(response)
                .hasSize(1)
                .allSatisfy { blogPost ->
                    assertThat(blogPost)
                        .usingRecursiveComparison()
                        .ignoringFields("title", "body")
                        .isEqualTo(expectedResponse)
                }
        }

        @Test
        internal fun `When retrieve all blog posts then resp is not empty and all posts have title and body`() {

            val responseList =
                Given {
                    spec(requestSpecification)
                } When {
                    get(BLOG_POSTS_ENDPOINT)
                } Then {
                    statusCode(HttpStatus.SC_OK)
                } Extract {
                    jsonPath().getList("", BlogPost::class.java)
                }

            assertThat(responseList)
                .isNotEmpty
                .allMatch { it.body.isNotBlank() and it.title.isNotBlank() }
        }
    }


    @Nested
    inner class CheckCreatingBlogPosts {
        @Test
        internal fun `When create blog post then got HTTP201 and BlogPost instance with id`() {
            val request = BlogPost(id = null, userId = 984, title = "Hello", body = "Hello, World!")

            val response =
                Given {
                    spec(requestSpecification)
                    body(request)
                } When {
                    post(BLOG_POSTS_ENDPOINT)
                } Then {
                    statusCode(HttpStatus.SC_CREATED)
                } Extract {
                    body().`as`(BlogPost::class.java)
                }

            assertThat(response)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(request)
        }

        @Test
        internal fun `When create empty blogpost then got server error`() {
            Given {
                spec(requestSpecification)
                body("qwerty")
            } When {
                post(BLOG_POSTS_ENDPOINT)
            } Then {
                statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR)
            }
        }
    }

    @Nested
    inner class CheckManagingUsers {
        @Test
        internal fun `When delete user then server response is HTTP 200`() {
            Given {
                spec(requestSpecification)
            } When {
                delete("${USERS_ENDPOINT}/1")
            } Then {
                statusCode(HttpStatus.SC_OK)
            }
        }
    }
}

data class BlogPost(val userId: Int, val id: Int?, val title: String, val body: String)
