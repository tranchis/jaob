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
package com.yoshtec.owl.annotations.classes;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * <a href="http://www.w3.org/2007/OWL/wiki/Syntax#Disjoint_Union_of_Class_Expression">
 * OWL Spec: DisjointUnion</a>
 * 
 * TODO: how use this in java?
 * 
 * <blockquote>
 * A disjoint union axiom DisjointClasses( C CE1 ... CEn ) states that a class C is a disjoint union of the class expressions CEi, 1 ≤ i ≤ n, all of which are mutually disjoint with each other. Such axioms are sometimes referred to as covering axioms, as they state that the extensions of all CEi exactly cover the extension of C. Thus, each instance of C must be an instance of exactly one CEi, and each instance of CEi must be an instance of C. Each such axiom is a syntactic shortcut for the following two axioms:
 * <br>
 * <code>
 * EquivalentClasses( C UnionOf( CE1 ... CEn ) )
 * DisjointClasses( CE1 ... CEn )
 * </code>
 * </blockquote>
 * 
 * @author Jonas von Malottki
 * 
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface OwlDisjointUnion {
	String[] disjointClasses();
}
