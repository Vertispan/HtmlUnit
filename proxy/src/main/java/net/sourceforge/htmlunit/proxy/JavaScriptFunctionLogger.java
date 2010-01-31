/*
 * Copyright (c) 2010 HtmlUnit team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sourceforge.htmlunit.proxy;

import net.sourceforge.htmlunit.corejs.javascript.Node;
import net.sourceforge.htmlunit.corejs.javascript.ast.Block;
import net.sourceforge.htmlunit.corejs.javascript.ast.FunctionNode;

/**
 * Logs function entry.
 * 
 * @author Ahmed Ashour
 * @version $Revision$
 */
public class JavaScriptFunctionLogger extends JavaScriptBeautifier {

    @Override
    protected void print(final Block node, final int depth) {
        if (node.getParent() instanceof FunctionNode) {
            final StringBuilder sb = getBuffer();
            final FunctionNode parent = (FunctionNode) node.getParent();
            if (parent.getFunctionName() != null) {
                makeIndent(depth);
                sb.append("{\n");
                makeIndent(depth + 1);
                sb.append("// entering function [");
                print(parent.getFunctionName(), 0);
                sb.append("]\n");
                for (final Node kid : node) {
                    print(kid, depth + 1);
                }
                makeIndent(depth);
                sb.append("}\n");
                return;
            }
        }
        super.print(node, depth);
    }

    
}
