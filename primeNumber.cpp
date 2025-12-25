#include <iostream>
#include <thread>
#include <vector>
#include <algorithm>

using namespace std; // std::count, std::endl

bool is_prime(int n) {
    if (n <= 1) return false;
    for (int i = 2; i * i <= n; i++) {
        if (n % i == 0) return false;
    }
    return true;
}

void list_of_primes(int start, int end, vector<int>& primes, mutex& m) {
    vector<int> local_primes;

    for (int i = start; i <= end; i++) {
        if (is_prime(i)) local_primes.push_back(i);
    }

    m.lock();
    for (int num : local_primes) {
        primes.push_back(num);
    }
    m.unlock();
}

vector<int> find_primes(int min, int max, int num_threads) {
    vector<int> primes;
    vector<thread> threads;
    mutex m;
    int range_size = (max - min) / num_threads;

    for (int i = 0; i < num_threads; i++) {
        int start = min + i * range_size;
        int end = (i == num_threads - 1) ? max : start + range_size - 1;
        threads.emplace_back(list_of_primes, start, end, ref(primes), ref(m));

    }
    for (auto& thread : threads) {
        thread.join();
    }

    stable_sort(primes.begin(), primes.end());
    return primes;
}

int main() {
    int start, end, num_threads;

    cout << "Start value: ";
    cin >> start;

    cout << "End value: ";
    cin >> end;

    cout << "Number of threads: ";
    cin >> num_threads;

    if (start > end) {
        int s = start;
        start = end;
        end = s;
    }

    vector<int> result = find_primes(start, end, num_threads);

    for (int num : result) {
        cout << num << " ";
    }
    cout << endl;

    return 0;
}
