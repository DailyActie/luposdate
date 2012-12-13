/**
 * Copyright (c) 2012, Institute of Information Systems (Sven Groppe), University of Luebeck
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 	- Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * 	  disclaimer.
 * 	- Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * 	  following disclaimer in the documentation and/or other materials provided with the distribution.
 * 	- Neither the name of the University of Luebeck nor the names of its contributors may be used to endorse or promote
 * 	  products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package lupos.event.producers.ebay.parser;

/**
 * Factory for incremental construction of a complex JSON object.
 */
public class JSMapFactory extends JSONFactory {

	/**
	 * JSON object to be constructed.
	 */
	private JSMap map = new JSMap();
	
	/**
	 * Latest string identifier.
	 */
	private String key = null;
	
	@Override
	public boolean append(char c) {
		boolean next = !this.finished;
		
		// If the 
		if (next) {
			/*
			 * ... delegate construction to the currently constructed JSON data structure
			 * contained by the object
			 */
			boolean goon = (this.factory == null) ? false : this.factory.append(c);
			
			/*
			 * If the currently constructed JSON data structure
			 * contained in the object is completed (or there isn't any) ...
			 */
			if (!goon) {
				/*
				 * ... and if the completed JSON data structure is a string identifier
				 * (or there isn't any) ...
				 */
				if (this.factory == null || this.key == null) {
					/*
					 * ... and if there a completed JSON data structure,
					 * set latest string identifier
					 */
					if (this.factory != null) {
						this.key = this.factory.create().toString();
						this.factory = null;
					}
					
					/*
					 * Begin construction of the JSON data structure identified
					 * by the lately set string identifier
					 */
					this.factory = JSONFactory.openWith(c);
				}
				// ... otherwise (the completed data structure isn't a identifier) ...
				else {
					/*
					 * ... add the pair of identifier and data structure
					 * to the object to be constructed
					 */
					this.map.set(this.key, this.factory.create());
					this.factory = null;
					this.key = null;
					
					// If c is termination character, finish construction
					if (c == '}') {
						this.finished = true;
					}
				}
			}
		}
		
		return next;
	}

	@Override
	public JSObject create() {
		return this.map;
	}
}
