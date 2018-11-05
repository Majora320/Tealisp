package org.majora320.tealisp.repl;

import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.majora320.tealisp.evaluator.Interpreter;
import org.majora320.tealisp.evaluator.LispException;
import org.majora320.tealisp.evaluator.LispObject;
import org.majora320.tealisp.lexer.LexException;
import org.majora320.tealisp.parser.ParseException;

import java.io.IOException;
import java.io.StringReader;

class Repl {
    public static void main(String[] args) {
        Interpreter interpreter = new Interpreter();

        LineReader reader = LineReaderBuilder.builder().build();
        while (true) {
            String line = null;

            try {
                line = reader.readLine("> ");
                LispObject result = interpreter.run(new StringReader(line));

                if (!(result instanceof LispObject.Void))
                    System.out.println(result);
            } catch (EndOfFileException | IOException e) {
                return;
            } catch (ParseException | LexException | LispException e) {
                e.printStackTrace();
            }
        }
    }
}
