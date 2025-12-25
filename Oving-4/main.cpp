#include <iostream>
#include <thread>
#include <condition_variable>
#include <list>
#include <mutex>
#include <vector>
#include <functional>

using namespace std;

class Workers {
    bool wait = false;
    mutex wait_mutex;
    condition_variable cv;
    list<function<void()>> functions;
    int threads;
    vector<thread> workerThreads;

public:
    Workers(int threads) : threads(threads) {}

    void start() {
        for (int i = 0; i < threads; i++) {
            workerThreads.emplace_back([this] {
                while (true) {
                    function<void()> task;
                    {
                        unique_lock<mutex> lock(wait_mutex);
                        cv.wait(lock, [this] { return !functions.empty() || wait; });

                        if (functions.empty() && wait) {
                            return;
                        }
                        task = *functions.begin();
                        functions.pop_front();
                    }
                    if (task) {
                        task();
                    }
                }
            });
        }
    }

    void post(function<void()> task) {
        {
            unique_lock<mutex> lock(wait_mutex);
            functions.push_back(task);
            cout << "Task posted" << endl;
        }
        cv.notify_one();
    }

    void post_timeout(function<void()> function, int timeout) {
        thread([this, function, timeout] {
            this_thread::sleep_for(chrono::milliseconds(timeout));
            post(function);
        }).detach();
    }

    void stop() {
        {
            unique_lock<mutex> lock(wait_mutex);
            wait = true;
        }
        cv.notify_all();
        for (auto &thread : workerThreads) {
            if (thread.joinable()) thread.join();
        }
    }

    void join() {
        for (auto &thread : workerThreads) {
            if (thread.joinable()) thread.join();
        }
    }

    ~Workers() {
        stop();
    }
};

int main() {
    Workers worker_threads(4);
    Workers event_loop(1);

    worker_threads.start();
    event_loop.start();

    worker_threads.post([] {
        cout << "Task A" << endl;
    });

    worker_threads.post([] {
        cout << "Task B" << endl;
    });

    event_loop.post([] {
        cout << "Task C" << endl;
    });

    event_loop.post([] {
        cout << "Task D " << endl;
    });

    event_loop.post_timeout([] {
        cout << "Task E (post timeout)" << endl;
    }, 2000);

    event_loop.post_timeout([] {
        cout << "Task F (post timeout)" << endl;
    }, 2000);

    worker_threads.join();
    event_loop.join();
    return 0;
}
