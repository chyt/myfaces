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

package org.apache.myfaces.cdi.scope;

import javax.faces.context.FacesContext;


/**
 * This class is used to destroy FacesScoped beans when FacesContext is destroyed.
 */
public class FacesScopeProvider
{
    
    public static void destroyBeans(FacesContext facesContext)
    {
        FacesScopeBeanHolder holder = new FacesScopeBeanHolder();
        holder.destroyBeans(facesContext);
    }
}
