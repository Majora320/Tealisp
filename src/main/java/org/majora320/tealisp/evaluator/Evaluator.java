package org.majora320.tealisp.evaluator;

import org.majora320.tealisp.parser.AstNode;

import java.util.List;

public class Evaluator {
    public LispObject evaluate(List<AstNode> nodes, StackFrame frame) {
        return new LispObject.Void();
    }
}