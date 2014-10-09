/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.yoshtec.owl.util;

import java.lang.reflect.Method;

import org.slf4j.Logger;
/**
 * Helps Debugging and retrieve Runtime Information 
 * for Java Objects at runtime
 * 
 * @author Jonas von Malottki
 *
 */
public class LogUtil {

	/**
	 * Logs the contents of an Object to the debug logger. 
	 * Uses reflection to get all getters and invokes them,
	 * logging the result to debug.
	 * 
	 * @param o Object to log
	 * @param log the Logger to log to
	 */
	public static void logObject(Object o, Logger log) {
		if(log.isDebugEnabled()){
			
			
			//Java reflection magic ;)
			Class<? extends Object> c = o.getClass();
			
			Method[] methods = c.getMethods();
			
			log.debug(c.getName());
			
			for (Method method : methods) {
				if(method.getName().startsWith("get")){
					StringBuffer sb = new StringBuffer();
					//sb.append(c.getName());
					//sb.append(".");
					sb.append(method.getName());
					sb.append(" : ");
					
					try {
						sb.append(method.invoke(o, (Object[])null));
					} catch (Exception e) {
						sb.append("EXCEPTION OCCURED");
					}
					//sb.append("\n");
					log.debug(sb.toString());
				}
			}
		}
	}
	
	
	/**
	 * Logs Information about the Object at hand to debug:
	 *
	 * <ul>
	 *  <li>Actual Class</li>
	 *  <li>Interfaces</li>
	 *  <li>Superclasses</li>
	 *  <li>Methods (without invoking them)</li>
	 * </ul>
	 *
	 * @see LogUtil#logObject(Object, Logger) 
	 *
	 * @param o
	 * @param log
	 */
	public static void logObjectInfo(Object o, Logger log){
		if(log.isDebugEnabled()){
			
			//Java reflection magic ;)
			Class<? extends Object> c = o.getClass();
			
			
			log.debug("CLASS");
			log.debug("C: " + c.getName());
			
			log.debug("INTERFACES:");
			for(Class<?> iface : c.getInterfaces()){
				log.debug("I: " + iface.getName());
			}
			
			{// unnamed Block
				log.debug("SUPERCLASSES:");
				Class<?> u = c;
				while(!u.getName().equals("java.lang.Object")){
					u = u.getSuperclass();
					log.debug("S: " + u.getName());
				}
			}
			
			log.debug("METHODS:");
			for (Method method : c.getMethods()) {
				log.debug(" M: {}",method.toGenericString());
			}
		}
	}
	
}
