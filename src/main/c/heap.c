int a[N];


// based on https://rosettacode.org/wiki/Sorting_algorithms/Heapsort#C

#include <stdio.h>

int max (int *a, int n, int parent) {
    int largest = parent;
    for(int child = (K * parent) + 1; child < (K * parent) + K + 1; child++) 
        if(child < n && a[child] > a[largest]) largest = child;

    return largest;
}

void downheap (int *a, int n, int i) {
    while (1) {
        int j = max(a, n, i);
        if (j == i) break;
        int t = a[i];
        a[i] = a[j];
        a[j] = t;
        i = j;
    }
}

void heapsort (int *a, int n) {
    int i;
    for (i = (n - 2) / K; i >= 0; i--) 
        downheap(a, n, i);
    
    for (i = 0; i < n; i++) {
        int t = a[n - i - 1];
        a[n - i - 1] = a[0];
        a[0] = t;
        downheap(a, n - i - 1, 0);
    }
}

int main () {

    for (int i = 0; i < REPETITIONS; i++) {
        for (int j = 0; j < N; j++) a[j] = j;
        heapsort(a, N);
    }

    return 0;
}
