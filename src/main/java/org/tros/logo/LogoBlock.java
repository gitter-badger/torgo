/*
 * Copyright 2015 Matthew Aguirre
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tros.logo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.commons.lang3.event.EventListenerSupport;
import org.tros.torgo.interpreter.CodeBlock;
import org.tros.torgo.interpreter.CodeFunction;
import org.tros.torgo.interpreter.InterpreterListener;
import org.tros.torgo.interpreter.InterpreterValue;
import org.tros.torgo.interpreter.ReturnValue;
import org.tros.torgo.interpreter.ReturnValue.ProcessResult;
import org.tros.torgo.interpreter.Scope;
import org.tros.torgo.interpreter.types.NullType;
import org.tros.utils.ImmutableHaltMonitor;

/**
 * Base component of Logo. This is a grouping of commands to run. A LogoBlock
 * can contain more LogoBlocks.
 *
 * @author matta
 */
abstract class LogoBlock implements CodeBlock {

    protected final ParserRuleContext ctx;
    private final ArrayList<CodeBlock> commands = new ArrayList<CodeBlock>();
    private final HashMap<String, CodeFunction> functions = new HashMap<String, CodeFunction>();
    protected final EventListenerSupport<InterpreterListener> listeners
            = EventListenerSupport.create(InterpreterListener.class);
    private final AtomicBoolean halted = new AtomicBoolean(false);

    private final HashMap<String, InterpreterValue> variables = new HashMap<String, InterpreterValue>();
    private CodeBlock parent;

    @Override
    public void addInterpreterListener(InterpreterListener listener) {
        listeners.addListener(listener);
    }

    @Override
    public void removeInterpreterListener(InterpreterListener listener) {
        listeners.removeListener(listener);
    }

    /**
     * Constructor
     *
     * @param ctx
     */
    protected LogoBlock(ParserRuleContext ctx) {
        this.ctx = ctx;
    }

    /**
     * Add a command to the list.
     *
     * @param command
     */
    @Override
    public final void addCommand(CodeBlock command) {
        if (!commands.contains(command)) {
            commands.add(command);
        }
    }

    /**
     * Add a collection of commands to the list.
     *
     * @param commands
     */
    @Override
    public final void addCommand(Collection<CodeBlock> commands) {
        this.commands.addAll(commands);
    }

    /**
     * Get the commands to interpret.
     *
     * @return
     */
    @Override
    public final CodeBlock[] getCommands() {
        return commands.toArray(new CodeBlock[]{});
    }

    /**
     * Is the current block halted.
     *
     * @return true if halted, false if the monitor is null or the monitor is
     * not halted.
     */
    @Override
    public boolean isHalted() {
        return halted.get();
    }

    /**
     * Process the statement(s)
     *
     * @param scope
     * @return true if we should continue, false otherwise
     */
    @Override
    public ReturnValue process(Scope scope) {
        AtomicBoolean success = new AtomicBoolean(true);
        AtomicBoolean stop = new AtomicBoolean(false);

        for (CodeBlock lc : commands) {
            if (success.get() && !stop.get()) {
                ReturnValue pr = lc.process(scope);
                if (pr.getResult() == ReturnValue.ProcessResult.HALT) {
                    success.set(false);
                } else if (pr.getResult() == ProcessResult.RETURN) {
                    stop.set(true);
                }
            }
        }

        ReturnValue.ProcessResult res = success.get() ? (stop.get() ? ReturnValue.ProcessResult.RETURN : ReturnValue.ProcessResult.SUCCESS) : ReturnValue.ProcessResult.HALT;
        return new ReturnValue(NullType.Instance, null, res);
    }

    /**
     * Look for a function with the specified name.
     *
     * @param name
     * @param scope
     * @return
     */
    public CodeFunction getFunction(String name, Scope scope) {
        return scope.getFunction(name);
    }

    /**
     * Look for a function with the specified name.
     *
     * @param name
     * @return
     */
    @Override
    public boolean hasFunction(String name) {
        return functions.containsKey(name);
    }

    /**
     * Look for a function with the specified name.
     *
     * @param name
     * @return
     */
    @Override
    public CodeFunction getFunction(String name) {
        return functions.get(name);
    }

    /**
     * Add a function to the current object. This allows for local declaration
     * of functions.
     *
     * @param function
     */
    @Override
    public void addFunction(CodeFunction function) {
        functions.put(function.getFunctionName(), function);
    }

    /**
     * Called when the halt monitor is halted.
     *
     * @param monitor
     */
    @Override
    public void halted(ImmutableHaltMonitor monitor) {
        halted.set(monitor.isHalted());
    }

    @Override
    public ParserRuleContext getParserRuleContext() {
        return ctx;
    }

    /**
     * Check for a variable in the block.
     *
     * @param name
     * @return
     */
    @Override
    public boolean hasVariable(String name) {
        return variables.containsKey(name);
    }

    /**
     * Set a variable in the block.
     *
     * @param name
     * @param value
     */
    @Override
    public void setVariable(String name, InterpreterValue value) {
        variables.put(name, value);
    }

    /**
     * Get the value of a variable in the block.
     *
     * @param name
     * @return
     */
    @Override
    public InterpreterValue getVariable(String name) {
        return variables.get(name);
    }

    /**
     * Get the lexical parent.
     *
     * @return
     */
    @Override
    public CodeBlock getParent() {
        return parent;
    }

    /**
     * Set the lexical parent.
     *
     * @param value
     */
    protected void setParent(CodeBlock value) {
        this.parent = value;
    }

    /**
     * Get the names of local variables.
     *
     * @return
     */
    @Override
    public Collection<String> localVariables() {
        return variables.keySet();
    }
}
