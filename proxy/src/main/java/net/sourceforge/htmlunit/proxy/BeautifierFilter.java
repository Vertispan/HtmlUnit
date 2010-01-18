package net.sourceforge.htmlunit.proxy;

import java.io.IOException;
import java.util.List;

import net.sourceforge.htmlunit.corejs.javascript.Node;
import net.sourceforge.htmlunit.corejs.javascript.Parser;
import net.sourceforge.htmlunit.corejs.javascript.ScriptRuntime;
import net.sourceforge.htmlunit.corejs.javascript.Token;
import net.sourceforge.htmlunit.corejs.javascript.ast.AstNode;
import net.sourceforge.htmlunit.corejs.javascript.ast.AstRoot;
import net.sourceforge.htmlunit.corejs.javascript.ast.Block;
import net.sourceforge.htmlunit.corejs.javascript.ast.CatchClause;
import net.sourceforge.htmlunit.corejs.javascript.ast.ExpressionStatement;
import net.sourceforge.htmlunit.corejs.javascript.ast.FunctionCall;
import net.sourceforge.htmlunit.corejs.javascript.ast.FunctionNode;
import net.sourceforge.htmlunit.corejs.javascript.ast.IfStatement;
import net.sourceforge.htmlunit.corejs.javascript.ast.Name;
import net.sourceforge.htmlunit.corejs.javascript.ast.NumberLiteral;
import net.sourceforge.htmlunit.corejs.javascript.ast.PropertyGet;
import net.sourceforge.htmlunit.corejs.javascript.ast.ReturnStatement;
import net.sourceforge.htmlunit.corejs.javascript.ast.Scope;
import net.sourceforge.htmlunit.corejs.javascript.ast.StringLiteral;
import net.sourceforge.htmlunit.corejs.javascript.ast.TryStatement;
import net.sourceforge.htmlunit.corejs.javascript.ast.UnaryExpression;
import sunlabs.brazil.filter.Filter;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.util.http.MimeHeaders;

/**
 * Beautifier filter.
 *
 * @author Ahmed Ashour
 */
public class BeautifierFilter implements Filter {

    @Override
    public byte[] filter(Request arg0, MimeHeaders arg1, byte[] arg2) {
        return null;
    }

    @Override
    public boolean shouldFilter(Request arg0, MimeHeaders arg1) {
        return false;
    }

    @Override
    public boolean init(Server arg0, String arg1) {
        return false;
    }

    @Override
    public boolean respond(Request arg0) throws IOException {
        return false;
    }

    String beautify(final String source) {
        Parser parser = new Parser(); 
        AstRoot root = parser.parse(source, "<cmd>", 0);
        StringBuilder builder = new StringBuilder();
        beautify(root, builder, 0);
        return builder.toString();
    }

    private void beautify(final Node node, final StringBuilder sb, int depth) {
        if (node instanceof AstRoot) {
            for (Node child : node) {
                beautify(child, sb, depth);
            }
        }
        else if (node instanceof FunctionNode) {
            FunctionNode f = (FunctionNode) node;
            makeIndent(depth, sb);
            sb.append("function");
            if (f.getFunctionName() != null) {
                sb.append(" ");
                beautify(f.getFunctionName(), sb, 0);
            }
            if (f.getParams().isEmpty()) {
                sb.append("() ");
            } else {
                sb.append("(");
                printList(f.getParams(), sb);
                sb.append(") ");
            }
            if (f.isExpressionClosure()) {
                sb.append(" ");
                beautify(f.getBody(), sb, 0);
            } else {
                int previousLength = sb.length();
                beautify(f.getBody(), sb, 0);
                sb.replace(previousLength, sb.length(), sb.substring(previousLength, sb.length()).trim());
            }
            if (f.getFunctionType() == FunctionNode.FUNCTION_STATEMENT) {
                sb.append("\n");
            }
        }
        else if (node instanceof Name) {
            Name name = (Name) node;
            makeIndent(depth, sb);
            sb.append(name.getIdentifier() == null ? "<null>" : name.getIdentifier());
        }
        else if (node instanceof Block) {
            makeIndent(depth, sb);
            sb.append("{\n");
            for (Node kid : node) {
                beautify(kid, sb, depth + 1);
            }
            makeIndent(depth, sb);
            sb.append("}\n");
        }
        else if (node instanceof IfStatement) {
            IfStatement s = (IfStatement) node;
            makeIndent(depth, sb);
            sb.append("if (");
            beautify(s.getCondition(), sb, 0);
            sb.append(") ");
            if (!(s.getThenPart() instanceof Block)) {
                sb.append("\n");
                makeIndent(depth, sb);
            }
            int previousLength = sb.length();
            beautify(s.getThenPart(), sb, depth);
            sb.replace(previousLength, sb.length(), sb.substring(previousLength, sb.length()).trim());

            if (s.getElsePart() instanceof IfStatement) {
                sb.append(" else ");
                previousLength = sb.length();
                beautify(s.getElsePart(), sb, depth);
                sb.replace(previousLength, sb.length(), sb.substring(previousLength, sb.length()).trim());
            } else if (s.getElsePart() != null) {
                sb.append(" else ");
                previousLength = sb.length();
                beautify(s.getElsePart(), sb, depth);
                sb.replace(previousLength, sb.length(), sb.substring(previousLength, sb.length()).trim());
            }
            sb.append("\n");
        }
        else if (node instanceof UnaryExpression) {
            UnaryExpression e = (UnaryExpression) node;
            makeIndent(depth, sb);
            if (!e.isPostfix()) {
                sb.append(AstNode.operatorToString(e.getType()));
                if (e.getType() == Token.TYPEOF || e.getType() == Token.DELPROP) {
                    sb.append(" ");
                }
            }
            beautify(e.getOperand(), sb, 0);
            if (e.isPostfix()) {
                sb.append(AstNode.operatorToString(e.getType()));
            }
        }
        else if (node instanceof Scope) {
            makeIndent(depth, sb);
            sb.append("{\n");
            for (Node kid : node) {
                beautify(kid, sb, depth + 1);
            }
            makeIndent(depth, sb);
            sb.append("}\n");
        }
        else if(node instanceof PropertyGet) {
            makeIndent(depth, sb);
            beautify(((PropertyGet) node).getLeft(), sb, 0);
            sb.append(".");
            beautify(((PropertyGet) node).getRight(), sb, 0);
        }
        else if (node instanceof TryStatement) {
            TryStatement s = (TryStatement) node;
            makeIndent(depth, sb);
            sb.append("try ");
            int previousLength = sb.length();
            beautify(s.getTryBlock(), sb, depth);
            sb.replace(previousLength, sb.length(), sb.substring(previousLength, sb.length()).trim());

            for (CatchClause cc : s.getCatchClauses()) {
                beautify(cc, sb, depth);
            }
            if (s.getFinallyBlock() != null) {
                sb.append(" finally ");
                beautify(s.getFinallyBlock(), sb, depth);
            }
        }
        else if (node instanceof ExpressionStatement) {
            beautify(((ExpressionStatement) node).getExpression(), sb, depth);
            sb.append(";\n");
        }
        else if (node instanceof FunctionCall) {
            FunctionCall f = (FunctionCall) node;
            makeIndent(depth, sb);
            beautify(f.getTarget(), sb, 0);
            sb.append("(");
            if (!f.getArguments().isEmpty()) {
                printList(f.getArguments(), sb);
            }
            sb.append(")");
        }
        else if (node instanceof CatchClause) {
            CatchClause c = (CatchClause) node;
            makeIndent(depth, sb);
            sb.append("catch (");
            beautify(c.getVarName(), sb, 0);
            if (c.getCatchCondition() != null) {
                sb.append(" if ");
                beautify(c.getCatchCondition(), sb, 0);
            }
            sb.append(") ");
            beautify(c.getBody(), sb, 0);
        }
        else if (node instanceof ReturnStatement) {
            makeIndent(depth, sb);
            sb.append("return");
            if (((ReturnStatement) node).getReturnValue() != null) {
                sb.append(" ");
                beautify(((ReturnStatement) node).getReturnValue(), sb, 0); 
            }
            sb.append(";\n");

        }
        else if (node instanceof StringLiteral) {
            StringLiteral s = (StringLiteral) node;
            makeIndent(depth, sb);
            sb.append(s.getQuoteCharacter())
            .append(ScriptRuntime.escapeString(s.getValue(), s.getQuoteCharacter()))
            .append(s.getQuoteCharacter());
        }
        else if (node instanceof NumberLiteral) {
            makeIndent(depth, sb);
            sb.append(((NumberLiteral) node).getValue() == null ? "<null>" : ((NumberLiteral) node).getValue());
        }
        else {
            throw new RuntimeException("Unknown " + node.getClass().getName());
        }
    }

    private static void makeIndent(int indent, StringBuilder builder) {
        for (int i = 0; i < indent; i++) {
            builder.append("  ");
        }
    }

    /**
     * Prints a comma-separated item list into a {@link StringBuilder}.
     * @param items a list to print
     * @param sb a {@link StringBuilder} into which to print
     */
    protected <T extends AstNode> void printList(List<T> items, StringBuilder sb) {
        int max = items.size();
        int count = 0;
        for (AstNode item : items) {
            beautify(item, sb, 0);
            if (count++ < max-1) {
                sb.append(", ");
            }
        }
    }
}
