/**
 * Copyright (c) 2007-2015, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
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
package lupos.engine.operators.messages;

import java.util.Collection;

import lupos.datastructures.bindings.BindingsFactory;
import lupos.engine.operators.BasicOperator;

/**
 * This message is used to inform the operators about the BindingsFactory to be used...
 *
 * @author groppe
 * @version $Id: $Id
 */
@SuppressWarnings("serial")
public class BindingsFactoryMessage extends Message {

	protected final BindingsFactory bindingsFactory;

	/**
	 * <p>Constructor for BindingsFactoryMessage.</p>
	 *
	 * @param bindingsFactory a {@link lupos.datastructures.bindings.BindingsFactory} object.
	 */
	public BindingsFactoryMessage(final BindingsFactory bindingsFactory){
		super();
		this.bindingsFactory = bindingsFactory;
	}

	/**
	 * <p>Constructor for BindingsFactoryMessage.</p>
	 *
	 * @param msg a {@link lupos.engine.operators.messages.Message} object.
	 */
	public BindingsFactoryMessage(final Message msg){
		super(msg);
		if(msg instanceof BindingsFactoryMessage){
			this.bindingsFactory = ((BindingsFactoryMessage)msg).bindingsFactory;
		} else {
			throw new RuntimeException("BindingsFactoryMessage expected, but got "+msg.getClass());
		}
	}

	/** {@inheritDoc} */
	@Override
	public Message postProcess(final BasicOperator op) {
		return op.postProcessMessage(this);
	}

	/** {@inheritDoc} */
	@Override
	public Message preProcess(final BasicOperator op) {
		return op.preProcessMessage(this);
	}

	/** {@inheritDoc} */
	@Override
	public Message merge(final Collection<Message> msgs, final BasicOperator op) {
		return op.mergeMessages(msgs, this);
	}

	/** {@inheritDoc} */
	@Override
	public Message clone() {
		return new BindingsFactoryMessage(this);
	}

	/**
	 * <p>Getter for the field <code>bindingsFactory</code>.</p>
	 *
	 * @return a {@link lupos.datastructures.bindings.BindingsFactory} object.
	 */
	public BindingsFactory getBindingsFactory(){
		return this.bindingsFactory;
	}
}
