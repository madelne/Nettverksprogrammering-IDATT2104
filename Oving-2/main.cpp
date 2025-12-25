#include <iostream>
#include <asio.hpp>
#include <asio/awaitable.hpp>
#include <asio/co_spawn.hpp>
#include <asio/detached.hpp>
#include <asio/use_awaitable.hpp>

using namespace asio;
using namespace std;
using tcp = ip::tcp;

string http_response(const string& request) {
    return "HTTP/1.1 200 OK\r\n",
           "Content-type: text/html\r\n"
              "Content-length: " + to_string(request.size()) + "\r\n"
                "\r\n" + request;
}

void handleClient(const shared_ptr<asio::ip::tcp::socket> &socket) {
    try {
        char buffer[1024] = {0};
        size_t bytes_read = co_await socket.async_read_some(buffer(buffer, sizeof(buffer)), use_awaitable);
        string request(buffer, bytes_read);
        cout << "Received request:\n" << request << endl;

        string response;

        if (request.find("GET / ") != string::npos) {
            response = http_response("<html><body><h1>First page</h1></body></html>");
        } else if (request.find("GET /page1") != string::npos) {
            response = http_response("<html><body><h1>Page 1</h1></body></html>");
        } else if (request.find("GET /page2") != string::npos) {
            response = http_response("<html><body><h1>Page 2</h1></body></html>");
        }

        co_await async_write(socket, buffer(response), use_awaitable);
    } catch (exception& e) {
    }
}