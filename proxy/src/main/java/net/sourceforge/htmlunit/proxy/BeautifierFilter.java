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
        print(root, builder, 0);
        return builder.toString();
    }

    private void print(final Node node, final StringBuilder sb, int depth) {
        if (node instanceof AstRoot) {
            print((AstRoot) node, sb, depth);
        }
        else if (node instanceof FunctionNode) {
            print((FunctionNode) node, sb, depth);
        }
        else if (node instanceof Name) {
            print((Name) node, sb, depth);
        }
        else if (node instanceof Block) {
            print((Block) node, sb, depth);
        }
        else if (node instanceof IfStatement) {
            print((IfStatement) node, sb, depth);
        }
        else if (node instanceof UnaryExpression) {
            print((UnaryExpression) node, sb, depth);
        }
        else if (node instanceof Scope) {
            print((Scope) node, sb, depth);
        }
        else if(node instanceof PropertyGet) {
            print((PropertyGet) node, sb, depth);
        }
        else if (node instanceof TryStatement) {
            print((TryStatement) node, sb, depth);
        }
        else if (node instanceof ExpressionStatement) {
            print((ExpressionStatement) node, sb, depth);
        }
        else if (node instanceof FunctionCall) {
            print((FunctionCall) node, sb, depth);
        }
        else if (node instanceof CatchClause) {
            print((CatchClause) node, sb, depth);
        }
        else if (node instanceof ReturnStatement) {
            print((ReturnStatement) node, sb, depth);
        }
        else if (node instanceof StringLiteral) {
            print((StringLiteral) node, sb, depth);
        }
        else if (node instanceof NumberLiteral) {
            print((NumberLiteral) node, sb, depth);
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
            print(item, sb, 0);
            if (count++ < max-1) {
                sb.append(", ");
            }
        }
    }

    protected void print(final AstRoot node, final StringBuilder sb, int depth) {
        for (final Node child : node) {
            print(child, sb, depth);
        }
    }

    protected void print(final FunctionNode node, final StringBuilder sb, int depth) {
        makeIndent(depth, sb);
        sb.append("function");
        if (node.getFunctionName() != null) {
            sb.append(" ");
            print(node.getFunctionName(), sb, 0);
        }
        if (node.getParams().isEmpty()) {
            sb.append("() ");
        } else {
            sb.append("(");
            printList(node.getParams(), sb);
            sb.append(") ");
        }
        if (node.isExpressionClosure()) {
            sb.append(" ");
            print(node.getBody(), sb, 0);
        } else {
            int previousLength = sb.length();
            print(node.getBody(), sb, 0);
            sb.replace(previousLength, sb.length(), sb.substring(previousLength, sb.length()).trim());
        }
        if (node.getFunctionType() == FunctionNode.FUNCTION_STATEMENT) {
            sb.append("\n");
        }
    }

    protected void print(final Name node, final StringBuilder sb, int depth) {
        makeIndent(depth, sb);
        sb.append(node.getIdentifier() == null ? "<null>" : node.getIdentifier());
    }

    protected void print(final Block node, final StringBuilder sb, int depth) {
        makeIndent(depth, sb);
        sb.append("{\n");
        for (final Node kid : node) {
            print(kid, sb, depth + 1);
        }
        makeIndent(depth, sb);
        sb.append("}\n");
    }

    protected void print(final IfStatement node, final StringBuilder sb, int depth) {
        makeIndent(depth, sb);
        sb.append("if (");
        print(node.getCondition(), sb, 0);
        sb.append(") ");
        if (!(node.getThenPart() instanceof Block)) {
            sb.append("\n");
            makeIndent(depth, sb);
        }
        int previousLength = sb.length();
        print(node.getThenPart(), sb, depth);
        sb.replace(previousLength, sb.length(), sb.substring(previousLength, sb.length()).trim());

        if (node.getElsePart() instanceof IfStatement) {
            sb.append(" else ");
            previousLength = sb.length();
            print(node.getElsePart(), sb, depth);
            sb.replace(previousLength, sb.length(), sb.substring(previousLength, sb.length()).trim());
        } else if (node.getElsePart() != null) {
            sb.append(" else ");
            previousLength = sb.length();
            print(node.getElsePart(), sb, depth);
            sb.replace(previousLength, sb.length(), sb.substring(previousLength, sb.length()).trim());
        }
        sb.append("\n");
    }

    protected void print(final UnaryExpression node, final StringBuilder sb, int depth) {
        makeIndent(depth, sb);
        if (!node.isPostfix()) {
            sb.append(AstNode.operatorToString(node.getType()));
            if (node.getType() == Token.TYPEOF || node.getType() == Token.DELPROP) {
                sb.append(" ");
            }
        }
        print(node.getOperand(), sb, 0);
        if (node.isPostfix()) {
            sb.append(AstNode.operatorToString(node.getType()));
        }
    }

    protected void print(final Scope node, final StringBuilder sb, int depth) {
        makeIndent(depth, sb);
        sb.append("{\n");
        for (Node kid : node) {
            print(kid, sb, depth + 1);
        }
        makeIndent(depth, sb);
        sb.append("}\n");
    }

    protected void print(final PropertyGet node, final StringBuilder sb, int depth) {
        makeIndent(depth, sb);
        print(node.getLeft(), sb, 0);
        sb.append(".");
        print(node.getRight(), sb, 0);
    }

    protected void print(final TryStatement node, final StringBuilder sb, int depth) {
        makeIndent(depth, sb);
        sb.append("try ");
        int previousLength = sb.length();
        print(node.getTryBlock(), sb, depth);
        sb.replace(previousLength, sb.length(), sb.substring(previousLength, sb.length()).trim());

        for (CatchClause cc : node.getCatchClauses()) {
            print(cc, sb, depth);
        }
        if (node.getFinallyBlock() != null) {
            sb.append(" finally ");
            print(node.getFinallyBlock(), sb, depth);
        }
    }

    protected void print(final ExpressionStatement node, final StringBuilder sb, int depth) {
        print(node.getExpression(), sb, depth);
        sb.append(";\n");
    }

    protected void print(final FunctionCall node, final StringBuilder sb, int depth) {
        makeIndent(depth, sb);
        print(node.getTarget(), sb, 0);
        sb.append("(");
        if (!node.getArguments().isEmpty()) {
            printList(node.getArguments(), sb);
        }
        sb.append(")");
    }

    protected void print(final CatchClause node, final StringBuilder sb, int depth) {
        makeIndent(depth, sb);
        sb.append("catch (");
        print(node.getVarName(), sb, 0);
        if (node.getCatchCondition() != null) {
            sb.append(" if ");
            print(node.getCatchCondition(), sb, 0);
        }
        sb.append(") ");
        print(node.getBody(), sb, 0);
    }

    protected void print(final ReturnStatement node, final StringBuilder sb, int depth) {
        makeIndent(depth, sb);
        sb.append("return");
        if (node.getReturnValue() != null) {
            sb.append(" ");
            print(node.getReturnValue(), sb, 0); 
        }
        sb.append(";\n");
    }

    protected void print(final StringLiteral node, final StringBuilder sb, int depth) {
        makeIndent(depth, sb);
        sb.append(node.getQuoteCharacter())
        .append(ScriptRuntime.escapeString(node.getValue(), node.getQuoteCharacter()))
        .append(node.getQuoteCharacter());
    }

    protected void print(final NumberLiteral node, final StringBuilder sb, int depth) {
        makeIndent(depth, sb);
        sb.append(node.getValue() == null ? "<null>" : node.getValue());
    }

}