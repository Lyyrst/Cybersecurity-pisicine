#include <stdio.h>
#include <string.h>
#include <stdbool.h>
#include <stdlib.h>

void ok() {
	puts("Good Job.");
	exit(0);
}

void no() {
	puts("Nope.");
	exit(1);
}

int main(void) {
	char str[110];
	char transformStr[9];
	int index1;
	int index2;
	char number[4];
	printf("Please enter key: ");
	if (scanf("%23s", str) != 1) {
		no();
	}
	if (str[0] != '0' || str[1] != '0') {
		no();
	}

	fflush(stdin);
	memset(transformStr, 0, 9);
	transformStr[0] = 'd';
	index1 = 2;
	index2 = 1;
	number[3] = '\0';

	while (strlen(transformStr) < 8 && index1 < (int)strlen(str))
	{
		number[0] = str[index1];
		number[1] = str[index1 + 1];
		number[2] = str[index1 + 2];
		transformStr[index2] = (char)atoi(number);
		index1 += 3;
		index2 += 1;
	}
	transformStr[index2] = '\0';

	if (strcmp(transformStr, "delabere")) {
		no();
	}
	ok();
	
	return (0);
}