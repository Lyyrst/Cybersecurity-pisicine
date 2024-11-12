#include <stdio.h>
#include <string.h>

int main(void) {
	char str[110];
	printf("Please enter key:");
	scanf("%s", str);
	if (strcmp(str, "__stack_check")) {
		printf("Nope.\n");
	} else {
		printf("Good job.\n");
	}
	return 0;
}
