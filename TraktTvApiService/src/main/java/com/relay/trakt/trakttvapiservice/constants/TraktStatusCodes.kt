package com.relay.trakt.trakttvapiservice.constants

enum class TraktStatusCodes(var code: Int, var msg: String) {
    C200(200, "Success"),
    C201(201, "Success - new resource created (POST)"),
    C204(204, "Success - no content to return (DELETE)"),
    C400(400, "Bad Request - request couldn't be parsed"),
    C401(401, "Unauthorized - OAuth must be provided"),
    C403(403, "Forbidden - invalid API key or unapproved app"),
    C404(404, "Not Found - method exists, but no record found"),
    C405(405, "Method Not Found - method doesn't exist"),
    C409(409, "Conflict - resource already created"),
    C412(412, "Precondition Failed - use application/json content type"),
    C422(422, "Unprocessable Entity - validation errors"),
    C429(429, "Rate Limit Exceeded"),
    C500(500, "Server Error - please open a support issue"),
    C503(503, "Service Unavailable - server overloaded (try again in 30s)"),
    C504(504, "Service Unavailable - server overloaded (try again in 30s)"),
    C520(520, "Service Unavailable - Cloudflare error"),
    C521(521, "Service Unavailable - Cloudflare error"),
    C522(522, "Service Unavailable - Cloudflare error");


    companion object {
        private val codeMsgMap = hashMapOf<Int, String>()

        init {
            values().forEach { statusCode ->
                codeMsgMap.put(statusCode.code, statusCode.msg)
            }
        }

        fun getMsg(statusCode: Int): String? {
            return codeMsgMap.get(statusCode)
        }
    }


}