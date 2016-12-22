/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.myfaces.component.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.faces.FacesException;
import javax.faces.component.ContextCallback;
import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;
import javax.faces.component.search.ComponentNotFoundException;
import javax.faces.component.search.SearchExpressionContext;
import javax.faces.component.search.SearchExpressionHandler;
import javax.faces.component.search.SearchExpressionHint;
import javax.faces.component.search.SearchKeywordContext;
import javax.faces.context.FacesContext;

/**
 *
 */
public class SearchExpressionHandlerImpl extends SearchExpressionHandler
{

    protected Set<SearchExpressionHint> addHint(Set<SearchExpressionHint> hints, SearchExpressionHint hint)
    {
        // already available
        if (hints.contains(hint))
        {
            return hints;
        }
        
        Set<SearchExpressionHint> newHints = new HashSet<SearchExpressionHint>(hints);
        newHints.add(hint);

        return newHints;
    }
    
    @Override
    public String resolveClientId(SearchExpressionContext searchExpressionContext, String expression)
    {
        FacesContext facesContext = searchExpressionContext.getFacesContext();
        UIComponent source = searchExpressionContext.getSource();
        CollectClientIdCallback callback = new CollectClientIdCallback();
        Set<SearchExpressionHint> hints = addHint(searchExpressionContext.getExpressionHints(),
                SearchExpressionHint.RESOLVE_SINGLE_COMPONENT);
        SearchExpressionHandler handler = facesContext.getApplication().getSearchExpressionHandler();
        if (handler.isPassthroughExpression(searchExpressionContext, expression))
        {
            return expression;
        }
        else
        {
            handler.invokeOnComponent(
                    searchExpressionContext, searchExpressionContext.getSource(), expression, callback);

            if (!callback.isClientIdFound() && hints != null && hints.contains(SearchExpressionHint.PARENT_FALLBACK))
            {
                callback.invokeContextCallback(facesContext, source.getParent());
            }
            if (!callback.isClientIdFound())
            {
                if (hints != null && hints.contains(SearchExpressionHint.IGNORE_NO_RESULT))
                {
                    //Ignore
                }
                else
                {
                    throw new ComponentNotFoundException("Cannot find component for expression \""
                        + expression + "\" referenced from \""
                        + searchExpressionContext.getSource().getClientId(facesContext) + "\".");
                }
            }
            return callback.getClientId();
        }
    }

    private static class CollectClientIdCallback implements ContextCallback
    {
        private String clientId = null;

        @Override
        public void invokeContextCallback(FacesContext context, UIComponent target)
        {
            if (clientId == null)
            {
                clientId = target.getClientId(context);
            }
        }

        private String getClientId()
        {
            return clientId;
        }

        private boolean isClientIdFound()
        {
            return clientId != null;
        }
    }

    @Override
    public List<String> resolveClientIds(SearchExpressionContext searchExpressionContext,
            String expressions)
    {
        FacesContext facesContext = searchExpressionContext.getFacesContext();
        UIComponent source = searchExpressionContext.getSource();
        CollectClientIdsCallback callback = new CollectClientIdsCallback();
        Set<SearchExpressionHint> hints = searchExpressionContext.getExpressionHints();
        SearchExpressionHandler handler = facesContext.getApplication().getSearchExpressionHandler();
        for (String expression : facesContext.getApplication().getSearchExpressionHandler().splitExpressions(
                        facesContext, expressions))
        {
            if (handler.isPassthroughExpression(searchExpressionContext, expression))
            {
                // It will be resolved in the client, just add the expression.
                callback.addClientId(expression);
            }
            else
            {
                handler.invokeOnComponent(
                        searchExpressionContext, searchExpressionContext.getSource(), expression, callback);
            }
        }
        if (!callback.isClientIdFound() && hints != null && hints.contains(SearchExpressionHint.PARENT_FALLBACK))
        {
            callback.invokeContextCallback(facesContext, source.getParent());
        }
        if (!callback.isClientIdFound())
        {
            if (hints != null && hints.contains(SearchExpressionHint.IGNORE_NO_RESULT))
            {
                //Ignore
            }
            else
            {
                throw new ComponentNotFoundException("Cannot find component for expression \""
                    + expressions + "\" referenced from \""
                    + searchExpressionContext.getSource().getClientId(facesContext) + "\".");
            }
        }
        return callback.getClientIds();
    }

    private static class CollectClientIdsCallback implements ContextCallback
    {
        private List<String> clientIds = null;

        @Override
        public void invokeContextCallback(FacesContext context, UIComponent target)
        {
            if (clientIds == null)
            {
                clientIds = new ArrayList<String>();
            }
            clientIds.add(target.getClientId(context));
        }

        private List<String> getClientIds()
        {
            return clientIds == null ? Collections.emptyList() : clientIds;
        }

        private boolean isClientIdFound()
        {
            return clientIds != null;
        }

        private void addClientId(String clientId)
        {
            if (clientIds == null)
            {
                clientIds = new ArrayList<String>();
            }
            clientIds.add(clientId);
        }
    }

    @Override
    public void resolveComponent(SearchExpressionContext searchExpressionContext, String expression,
        ContextCallback callback)
    {
        FacesContext facesContext = searchExpressionContext.getFacesContext();
        SingleInvocationCallback checkCallback = new SingleInvocationCallback(callback);
        Set<SearchExpressionHint> hints = addHint(searchExpressionContext.getExpressionHints(),
                SearchExpressionHint.RESOLVE_SINGLE_COMPONENT);
        facesContext.getApplication().getSearchExpressionHandler().invokeOnComponent(
                searchExpressionContext, searchExpressionContext.getSource(), expression, checkCallback);

        if (!checkCallback.isInvoked() && hints != null && hints.contains(SearchExpressionHint.PARENT_FALLBACK))
        {
            checkCallback.invokeContextCallback(facesContext, searchExpressionContext.getSource().getParent());
        }
        if (!checkCallback.isInvoked())
        {
            if (hints != null && hints.contains(SearchExpressionHint.IGNORE_NO_RESULT))
            {
                //Ignore
            }
            else
            {
                throw new ComponentNotFoundException("Cannot find component for expression \""
                    + expression + "\" referenced from \""
                    + searchExpressionContext.getSource().getClientId(facesContext) + "\".");
            }
        }
    }

    private static class SingleInvocationCallback implements ContextCallback
    {
        private boolean invoked;

        private final ContextCallback innerCallback;

        public SingleInvocationCallback(ContextCallback innerCallback)
        {
            this.innerCallback = innerCallback;
            this.invoked = false;
        }

        @Override
        public void invokeContextCallback(FacesContext context, UIComponent target)
        {
            if (!isInvoked())
            {
                try
                {
                    innerCallback.invokeContextCallback(context, target);
                }
                finally
                {
                    invoked = true;
                }
            }
        }

        public boolean isInvoked()
        {
            return invoked;
        }
    }

    @Override
    public void resolveComponents(SearchExpressionContext searchExpressionContext, String expressions,
        ContextCallback callback)
    {
        FacesContext facesContext = searchExpressionContext.getFacesContext();
        MultipleInvocationCallback checkCallback = new MultipleInvocationCallback(callback);
        Set<SearchExpressionHint> hints = searchExpressionContext.getExpressionHints();
        for (String expression : facesContext.getApplication().getSearchExpressionHandler().splitExpressions(
                facesContext, expressions))
        {
            facesContext.getApplication().getSearchExpressionHandler().invokeOnComponent(
                    searchExpressionContext, searchExpressionContext.getSource(), expression, checkCallback);
        }
        // ...
        if (!checkCallback.isInvoked() && hints != null && hints.contains(SearchExpressionHint.PARENT_FALLBACK))
        {
            checkCallback.invokeContextCallback(facesContext, searchExpressionContext.getSource().getParent());
        }
        if (!checkCallback.isInvoked())
        {
            if (hints != null && hints.contains(SearchExpressionHint.IGNORE_NO_RESULT))
            {
                //Ignore
            }
            else
            {
                throw new ComponentNotFoundException("Cannot find component for expression \""
                    + expressions + "\" referenced from \""
                    + searchExpressionContext.getSource().getClientId(facesContext) + "\".");
            }
        }
    }

    private static class MultipleInvocationCallback implements ContextCallback
    {
        private boolean invoked;

        private final ContextCallback innerCallback;

        public MultipleInvocationCallback(ContextCallback innerCallback)
        {
            this.innerCallback = innerCallback;
            this.invoked = false;
        }

        @Override
        public void invokeContextCallback(FacesContext context, UIComponent target)
        {
            try
            {
                innerCallback.invokeContextCallback(context, target);
            }
            finally
            {
                invoked = true;
            }
        }

        public boolean isInvoked()
        {
            return invoked;
        }
    }

    @Override
    public void invokeOnComponent(final SearchExpressionContext searchExpressionContext,
            UIComponent previous, String topExpression, ContextCallback topCallback)
    {
        // Command pattern to apply the keyword or command to the base and then invoke the callback
        FacesContext facesContext = searchExpressionContext.getFacesContext();

        UIComponent currentBase = previous;

        //Step 1: find base
        //  Case ':' (root)
        char separatorChar = facesContext.getNamingContainerSeparatorChar();
        if (topExpression.charAt(0) == separatorChar)
        {
            UIComponent findBase;
            findBase = SearchComponentUtils.getRootComponent(currentBase);
            facesContext.getApplication().getSearchExpressionHandler().invokeOnComponent(
                    searchExpressionContext, findBase, topExpression.substring(1), topCallback);
            return;
        }

        //Step 2: Once you have a base where you can start, apply an expression
        if (topExpression.charAt(0) == KEYWORD_PREFIX.charAt(0))
        {
            // A keyword means apply a command over the current source using an expression and the result must be
            // feedback into the algorithm.
            final UIComponent base = currentBase;

            String command = extractKeyword(topExpression, 1, separatorChar);
            final String remaining =
                    command.length()+1 < topExpression.length() ?
                        topExpression.substring(1+command.length()+1) : null;

            final ContextCallback parentCallback = topCallback;
            final SearchExpressionHandler currentInstance =
                    facesContext.getApplication().getSearchExpressionHandler();

            // If the keyword is @child, @composite, @form, @namingcontainer, @next, @none, @parent, @previous,
            // @root, @this ,  all commands change the source to be applied the action
            
            if (remaining != null)
            {
                if (facesContext.getApplication().getSearchKeywordResolver().isLeaf(searchExpressionContext, command))
                {
                    throw new FacesException("Expression cannot have keywords or ids at the right side: "+command);
                }
                this.applyKeyword(searchExpressionContext, base, command, remaining, new ContextCallback()
                    {
                        @Override
                        public void invokeContextCallback(FacesContext facesContext, UIComponent target)
                        {
                            currentInstance.invokeOnComponent(
                                    searchExpressionContext, target, remaining, parentCallback);
                        }
                    });
            }
            else
            {
                // Command completed, apply parent callback
                this.applyKeyword(searchExpressionContext, base, command, null, parentCallback);
            }
        }
        else
        {

            //Split expression into tokens and apply loop
            String nextExpression = null;
            String expression;
            if (topExpression.indexOf(":@") > 0)
            {
                int idx = topExpression.indexOf(":@");
                nextExpression = topExpression.substring(idx+1);
                expression = topExpression.substring(0,idx);
            }
            else
            {
                expression = topExpression;
            }

            // Use findComponent(...) passing the expression provided
            UIComponent target = currentBase.findComponent(expression);
            if (target == null)
            {
                // If no component is found ...
                // First try to find the base component.

                // Extract the base id from the expression string
                int idx = expression.indexOf(separatorChar);
                String base = idx > 0 ? expression.substring(0, idx) : expression;

                // From the context component clientId, check if the base is part of the clientId
                String contextClientId = currentBase.getClientId(facesContext);
                int startCommon = contextClientId.lastIndexOf(base+facesContext.getNamingContainerSeparatorChar());
                if (startCommon >= 0
                    && (startCommon == 0 || contextClientId.charAt(startCommon-1) == separatorChar )
                    && (startCommon+base.length() <= contextClientId.length()-1 ||
                        contextClientId.charAt(startCommon+base.length()+1) == separatorChar ))
                {
                    // If there is a match, try to find a the first parent component whose id is equals to
                    // the base id
                    UIComponent parent = currentBase;
                    while (parent != null )
                    {
                        if (base.equals(parent.getId()) && parent instanceof NamingContainer)
                        {
                            break;
                        }
                        else
                        {
                            parent = parent.getParent();
                        }
                    }

                    // if a base component is found ...
                    if (parent != null)
                    {
                        target = parent.findComponent(expression);
                        if (target == null && !searchExpressionContext.getExpressionHints().contains(
                                SearchExpressionHint.SKIP_VIRTUAL_COMPONENTS))
                        {
                            contextClientId = parent.getClientId(facesContext);
                            // If no component is found,
                            String targetClientId = contextClientId.substring(0, startCommon+base.length()) +
                                    expression.substring(base.length());

                            final SearchExpressionHandler currentHandler =
                                    facesContext.getApplication().getSearchExpressionHandler();

                            if (nextExpression != null)
                            {
                                final String childExpression = nextExpression;

                                parent.invokeOnComponent(facesContext, targetClientId, new ContextCallback(){
                                    public void invokeContextCallback(FacesContext context, UIComponent target)
                                    {
                                        currentHandler.invokeOnComponent(
                                                searchExpressionContext, target, childExpression, topCallback);
                                    }
                                });
                            }
                            else
                            {
                                parent.invokeOnComponent(facesContext, targetClientId, topCallback);
                            }
                            return;
                        }
                    }
                }
            }
            if (target != null)
            {
                currentBase = target;
            }
            if (currentBase != null)
            {
                topCallback.invokeContextCallback(facesContext, currentBase);
            }
        }
    }

    protected void applyKeyword(SearchExpressionContext searchExpressionContext, UIComponent last,
                             String command, String remainingExpression, ContextCallback topCallback)
    {
        // take the command and resolve it using the chain of responsibility pattern.
        SearchKeywordContext searchContext = new SearchKeywordContext(searchExpressionContext.getFacesContext());
        searchContext.setSearchExpressionContext(searchExpressionContext);
        searchContext.setTopCallback(topCallback);
        searchContext.setRemainingExpression(remainingExpression);
        searchExpressionContext.getFacesContext().getApplication()
                .getSearchKeywordResolver().resolve(searchContext, last, command);
    }

    @Override
    public boolean isPassthroughExpression(SearchExpressionContext searchExpressionContext, String topExpression)
    {
        FacesContext facesContext = searchExpressionContext.getFacesContext();
        // Command pattern to apply the keyword or command to the base and then invoke the callback
        boolean passthrough = false;
        //Step 1: find base
        //  Case ':' (root)
        char separatorChar = facesContext.getNamingContainerSeparatorChar();
        if (topExpression.charAt(0) == separatorChar)
        {
            //return facesContext.getApplication().getSearchExpressionHandler().isPassthroughExpression(
            //        searchExpressionContext, topExpression.substring(1));
            // only keywords are passthrough expressions.
            return false;
        }

        //Step 2: Once you have a base where you can start, apply an expression
        if (topExpression.charAt(0) == KEYWORD_PREFIX.charAt(0))
        {
            // A keyword means apply a command over the current source using an expression and the result must be
            // feedback into the algorithm.

            String command = extractKeyword(topExpression, 1, separatorChar);
            final String remaining =
                    command.length()+1 < topExpression.length() ?
                        topExpression.substring(1+command.length()+1) : null;

            final SearchExpressionHandler currentInstance =
                    facesContext.getApplication().getSearchExpressionHandler();

            // If the keyword is @child, @composite, @form, @namingcontainer, @next, @none, @parent, @previous,
            // @root, @this ,  all commands change the source to be applied the action
            passthrough = facesContext.getApplication().getSearchKeywordResolver().isPassthrough(
                    searchExpressionContext, command);

            if (passthrough)
            {
                return remaining != null ?
                        currentInstance.isPassthroughExpression(searchExpressionContext, remaining) : true;
            }
            else
            {
                return false;
            }
        }
        else
        {
            // Only keywords are valid to be passthrough. If it contains a chain of ids, this can only be resolved
            // server side, because the tree structure and related clientId logic is only available server side.
            return false;
        }
    }

    public boolean isValidExpression(SearchExpressionContext searchExpressionContext, String topExpression)
    {
        FacesContext facesContext = searchExpressionContext.getFacesContext();
        // Command pattern to apply the keyword or command to the base and then invoke the callback
        boolean isValid = true;
        //Step 1: find base
        //  Case ':' (root)
        char separatorChar = facesContext.getNamingContainerSeparatorChar();
        if (topExpression.charAt(0) == separatorChar)
        {
            return facesContext.getApplication().getSearchExpressionHandler().isValidExpression(
                    searchExpressionContext, topExpression.substring(1));
        }

        //Step 2: Once you have a base where you can start, apply an expression
        if (topExpression.charAt(0) == KEYWORD_PREFIX.charAt(0))
        {
            // A keyword means apply a command over the current source using an expression and the result must be
            // feedback into the algorithm.

            String command = extractKeyword(topExpression, 1, separatorChar);
            final String remaining =
                    command.length()+1 < topExpression.length() ?
                        topExpression.substring(1+command.length()+1) : null;

            final SearchExpressionHandler currentInstance =
                    facesContext.getApplication().getSearchExpressionHandler();

            // If the keyword is @child, @composite, @form, @namingcontainer, @next, @none, @parent, @previous,
            // @root, @this ,  all commands change the source to be applied the action
            isValid = facesContext.getApplication().getSearchKeywordResolver().matchKeyword(
                    searchExpressionContext, command);
            if (remaining != null)
            {
                if (facesContext.getApplication().getSearchKeywordResolver().isLeaf(
                    searchExpressionContext, command))
                {
                    isValid = false;
                }
                return !isValid ? false : currentInstance.isValidExpression(searchExpressionContext, remaining);
            }
        }
        else
        {
            //Split expression into tokens and apply loop
            String nextExpression = null;
            String expression = null;
            if (topExpression.indexOf(":@") > 0)
            {
                int idx = topExpression.indexOf(":@");
                nextExpression = topExpression.substring(idx+1);
                expression = topExpression.substring(0,idx);
            }
            else
            {
                expression = topExpression;
            }

            //Check expression
            for (int i = 0; i < expression.length(); i++)
            {
                char c = expression.charAt(i);
                if (Character.isLetterOrDigit(c) || c == '-' || c == '_' || c == separatorChar)
                {
                    //continue
                }
                else
                {
                    isValid = false;
                }
            }

            if (nextExpression != null)
            {
                return !isValid ? false : facesContext.getApplication().getSearchExpressionHandler()
                    .isValidExpression(searchExpressionContext, nextExpression);
            }
        }
        return isValid;
    }

    private static String extractKeyword(String expression, int startIndex, char separatorChar)
    {
        int parenthesesCounter = -1;
        int count = -1;
        for (int i = startIndex; i < expression.length(); i++)
        {
            char c = expression.charAt(i);
            if (c == '(')
            {
                if (parenthesesCounter == -1)
                {
                    parenthesesCounter = 0;
                }
                parenthesesCounter++;
            }
            if (c == ')')
            {
                parenthesesCounter--;
            }
            if (parenthesesCounter == 0)
            {
                //Close first parentheses
                count = i+1;
                break;
            }
            if (parenthesesCounter == -1)
            {
                if (c == separatorChar)
                {
                    count = i;
                    break;
                }
            }
        }
        if (count == -1)
        {
            return expression.substring(startIndex);
        }
        else
        {
            return expression.substring(startIndex, count);
        }
    }

    @Override
    public String[] splitExpressions(FacesContext context, String expressions)
    {
        // split expressions by blank or comma (and ignore blank and commas inside brackets)
        String[] splittedExpressions = split(expressions, EXPRESSION_SEPARATOR_CHARS);
        return splittedExpressions;
    }

    private static String[] split(String value, char... separators)
    {
        if (value == null)
        {
            return null;
        }

        List<String> tokens = new ArrayList<String>();
        StringBuilder buffer = new StringBuilder();

        int parenthesesCounter = 0;

        char[] charArray = value.toCharArray();

        for (char c : charArray)
        {
            if (c == '(')
            {
                parenthesesCounter++;
            }

            if (c == ')')
            {
                parenthesesCounter--;
            }

            if (parenthesesCounter == 0)
            {
                boolean isSeparator = false;
                for (char separator : separators)
                {
                    if (c == separator)
                    {
                        isSeparator = true;
                    }
                }

                if (isSeparator)
                {
                    // lets add token inside buffer to our tokens
                    tokens.add(buffer.toString());
                    // now we need to clear buffer
                    buffer.delete(0, buffer.length());
                }
                else
                {
                    buffer.append(c);
                }
            }
            else
            {
                buffer.append(c);
            }
        }

        // lets not forget about part after the separator
        tokens.add(buffer.toString());

        return tokens.toArray(new String[tokens.size()]);
    }

}