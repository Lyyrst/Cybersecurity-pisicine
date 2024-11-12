#include <string.h>
#include <stdlib.h>
#include <stdio.h>

int	___syscall_malloc()
{
	puts("Nope.");
	exit(1);
}

int	_____syscall_malloc()
{
	return puts("Good job.");
}

int	main()
{
	int ret;
	int i;
	size_t index;
	char inputStr[31];
	char convertedStr[9];
	char numbers[4];

	printf("Please enter key: ");
	ret = scanf("%23s", inputStr);

	if (ret != 1 || inputStr[0] != '4' || inputStr[1] != '2') {
		___syscall_malloc();
	}

	fflush(stdin);
	memset(convertedStr, 0, sizeof(convertedStr));
	convertedStr[0] = '*';
	numbers[3] = '\0';
	index = 2;
	for(i = 1; ; ++i) {
		if (strlen(convertedStr) >= 8 || index >= strlen(inputStr))
			break;
		numbers[0] = inputStr[index];
		numbers[1] = inputStr[index + 1];
		numbers[2] = inputStr[index + 2];
		convertedStr[i] = atoi(numbers);
		index += 3;
	}
	convertedStr[i] = '\0';
	switch (strcmp(convertedStr, "********")) {
		case -2:
			___syscall_malloc();
			break;
		case -1:
			___syscall_malloc();
			break;
		case 0:
			_____syscall_malloc();
			break;
		case 1:
			___syscall_malloc();
			break;
		case 2:
			___syscall_malloc();
			break;
		case 3:
			___syscall_malloc();
			break;
		case 4:
			___syscall_malloc();
			break;
		case 5:
			___syscall_malloc();
			break;
		case 115:
			___syscall_malloc();
			break;
		default:
			___syscall_malloc();
			break;
	}
	return 0;
}