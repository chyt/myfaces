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
package org.apache.myfaces.view.facelets;

import java.io.IOException;
import java.net.URL;

import javax.el.ELException;
import javax.el.ExpressionFactory;
import javax.faces.FacesException;
import javax.faces.application.Resource;
import javax.faces.component.UIComponent;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.FaceletException;

import org.apache.myfaces.view.facelets.impl.DefaultFaceletFactory;

/**
 * @author Leonardo Uribe (latest modification by $Author: lu4242 $)
 * @version $Revision: 947351 $ $Date: 2010-05-22 19:19:48 -0500 (Sáb, 22 May 2010) $
 * @since 2.0.1
 */
public abstract class AbstractFacelet extends Facelet
{
    
    /**
     * Return this Facelet's ExpressionFactory instance
     * 
     * @return internal ExpressionFactory instance
     */
    public abstract ExpressionFactory getExpressionFactory();
    
    /**
     * Used for delegation by the DefaultFaceletContext. First pulls the URL from {@link #getRelativePath(String)
     * getRelativePath(String)}, then calls {@link #include(FaceletContext, UIComponent, URL) include(FaceletContext,
     * UIComponent, URL)}.
     * 
     * @see FaceletContext#includeFacelet(UIComponent, String)
     * @param ctx
     *            FaceletContext to pass to the included Facelet
     * @param parent
     *            UIComponent to apply changes to
     * @param path
     *            relative path to the desired Facelet from the FaceletContext
     * @throws IOException
     * @throws FacesException
     * @throws FaceletException
     * @throws ELException
     */
    public abstract void include(AbstractFaceletContext ctx, UIComponent parent, String path) throws IOException, FacesException,
            FaceletException, ELException;
    
    /**
     * Grabs a DefaultFacelet from referenced DefaultFaceletFacotry
     * 
     * @see DefaultFaceletFactory#getFacelet(URL)
     * @param ctx
     *            FaceletContext to pass to the included Facelet
     * @param parent
     *            UIComponent to apply changes to
     * @param url
     *            URL source to include Facelet from
     * @throws IOException
     * @throws FacesException
     * @throws FaceletException
     * @throws ELException
     */
    public abstract void include(AbstractFaceletContext ctx, UIComponent parent, URL url) throws IOException, FacesException,
            FaceletException, ELException;
    
    /**
     * Return the alias name for error messages and logging
     * 
     * @return alias name
     */
    public abstract String getAlias();
    
    public abstract void applyCompositeComponent(AbstractFaceletContext ctx, UIComponent parent, Resource resource) throws IOException, FacesException,
            FaceletException, ELException;
    
    public abstract boolean isBuildingCompositeComponentMetadata();
}
