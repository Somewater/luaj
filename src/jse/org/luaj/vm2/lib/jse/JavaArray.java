/*******************************************************************************
* Copyright (c) 2011 Luaj.org. All rights reserved.
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in
* all copies or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
* THE SOFTWARE.
******************************************************************************/
package org.luaj.vm2.lib.jse;

import java.lang.reflect.Array;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaUserdata;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;

/**
 * LuaValue that represents a Java instance of array type.
 * <p>
 * Can get elements by their integer key index, as well as the length.
 * <p>
 * This class is not used directly.  
 * It is returned by calls to {@link CoerceJavaToLua#coerce(Object)} 
 * when an array is supplied.
 * @see CoerceJavaToLua
 * @see CoerceLuaToJava
 */
class JavaArray extends LuaUserdata {

	private static final class LenFunction extends OneArgFunction {
		public LuaValue call(LuaValue u) {
			return LuaValue.valueOf(Array.getLength(((LuaUserdata)u).m_instance));
		}
	}

	static final LuaValue LENGTH = valueOf("length");
	static final LuaValue COUNT_LEN = valueOf("Count");
	private static final int ZERO_INDEX = 0; // "0" for zero-based index, "1" for one-based index
	
	static final LuaTable array_metatable;
	static {
		array_metatable = new LuaTable();
		array_metatable.rawset(LuaValue.LEN, new LenFunction());
		assert(ZERO_INDEX == 0 || ZERO_INDEX == 1);
	}
	
	JavaArray(Object instance) {
		super(instance);
		setmetatable(array_metatable);
	}
	
	public LuaValue get(LuaValue key) {
		if ( key.equals(LENGTH) || key.equals(COUNT_LEN))
			return valueOf(Array.getLength(m_instance));
		if ( key.isint() ) {
			int i = key.toint() - ZERO_INDEX;
			return i>=0 && i<Array.getLength(m_instance)?
				CoerceJavaToLua.coerce(Array.get(m_instance,key.toint()-ZERO_INDEX)):
				NIL;
		}
		return super.get(key);
	}

	public void set(LuaValue key, LuaValue value) {
		if ( key.isint() ) {
			int i = key.toint() - ZERO_INDEX;
			if ( i>=0 && i<Array.getLength(m_instance) )
				Array.set(m_instance,i,CoerceLuaToJava.coerce(value, m_instance.getClass().getComponentType()));
			else if ( m_metatable==null || ! settable(this,key,value) )
					error("array index out of bounds");
		}
		else
			super.set(key, value);
	} 	
}
