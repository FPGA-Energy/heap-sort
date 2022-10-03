// based on https://rosettacode.org/wiki/Sorting_algorithms/Heapsort#C
#include <stdio.h>

#define K 8

int max (int *a, int n, int parent) {
    int smallest = parent;
    for(int child = (K * parent) + 1; child < (K * parent) + K + 1; child++) 
        if(child < n && a[child] < a[smallest]) smallest = child;

    return smallest;
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
    int a[] = {4, 65, 2, -31, 0, 99, 2, 83, 782, 1};
    int n = sizeof a / sizeof a[0];
    int i;

    for (i = 0; i < n; i++)
        printf("%d%s", a[i], i == n - 1 ? "\n" : " ");

    heapsort(a, n);

    for (i = 0; i < n; i++)
        printf("%d%s", a[i], i == n - 1 ? "\n" : " ");
        
    return 0;
}
