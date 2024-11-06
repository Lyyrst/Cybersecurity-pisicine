# Stockholm - Ransomware Simulation Program

**Stockholm** is a program designed to simulate a ransomware infection on Linux. It encrypts files in the
`$HOME/infection` folder using a specified key and can reverse the encryption.

## Features

- Encrypts files in `$HOME/infection` directory.
- Works with files commonly affected by ransomware.
- Uses AES encryption with a 16-character key minimum.
- Renames files by adding the `.ft` extension.
- Reverses encryption with the provided key.
- Runs in silent mode with no output using `-s`.

## Usage

### Command-Line Options:

- `-h` or `--help`: Displays help information.
- `-v` or `--version`: Displays the program version.
- `-r` or `--reverse <key>`: Reverses the encryption using the provided key.
- `-s` or `--silent`: Runs in silent mode with no output.

### Examples:

- Encrypt files :

```bash
  MY_VAR=<key> java -jar target/stockholm-1.0-SNAPSHOT-jar-with-dependencies.jar
```

- Encrypt files in silent mode :

```bash
  MY_VAR=<key> java -jar target/stockholm-1.0-SNAPSHOT-jar-with-dependencies.jar -s
```

- Reverse encryption :

```bash
  java -jar target/stockholm-1.0-SNAPSHOT-jar-with-dependencies.jar -r <key>
```

## Compilation

Clone the repository.  
In the root folder, run:

```bash
make
```

### Key generation

The key used for encryption must be at least 16 characters long. Ensure the key is secure and private.

### Security Warning

This program simulates ransomware for educational purposes. Only use in a controlled environment and ensure backups of any important files.
