package main

import (
	"bufio"
	"fmt"
	"os"
	"regexp"
)

func main() {
	// Define the regular expressions for the patterns to be replaced
	reDoubleDash := regexp.MustCompile(`--[^\t]*\t`)
	reDoubleSlash := regexp.MustCompile(`//[^\t]*\t`)

	scanner := bufio.NewReader(os.Stdin)
	writer := bufio.NewWriter(os.Stdout)
	defer writer.Flush()

	for {
		line, err := scanner.ReadString('\n')
		if err != nil {

			if err.Error() == "EOF" {
				fmt.Fprintf(os.Stderr, "EOF reached\n")
				break
			} else {
				fmt.Fprintf(os.Stderr, "Error reading input: %v\n", err)
				return
			}
		}

		// Replace the patterns with the empty string
		line = reDoubleDash.ReplaceAllString(line, "\t")
		line = reDoubleSlash.ReplaceAllString(line, "\t")

		// Write the modified line to the output
		fmt.Fprint(writer, line)
	}

}
