use regex::Regex;
use std::io::{self, BufRead, Write};

fn main() {
    // Define the regular expressions for the patterns to be replaced
    let re_double_dash = Regex::new(r"--[^\t]*\t").unwrap();
    let re_double_slash = Regex::new(r"//[^\t]*\t").unwrap();

    let stdin = io::stdin();
    let stdout = io::stdout();
    let mut writer = io::BufWriter::new(stdout.lock());

    for line in stdin.lock().lines() {
        match line {
            Ok(mut line) => {
                // Replace the patterns with the empty string
                line = re_double_dash.replace_all(&line, "\t").to_string();
                line = re_double_slash.replace_all(&line, "\t").to_string();

                // Write the modified line to the output
                writeln!(writer, "{}", line).expect("Error writing to output");
            }
            Err(err) => {
                if err.kind() == io::ErrorKind::UnexpectedEof {
                    eprintln!("EOF reached");
                    break;
                } else {
                    eprintln!("Error reading input: {:?}", err);
                    return;
                }
            }
        }
    }

    writer.flush().expect("Error flushing output");
}

