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
package org.tros.torgo.viz;

import org.tros.utils.swing.NamedWindow;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.text.MessageFormat;
import static javax.swing.WindowConstants.HIDE_ON_CLOSE;
import org.tros.torgo.interpreter.CodeBlock;
import org.tros.torgo.Controller;
import org.tros.torgo.interpreter.InterpreterListener;
import org.tros.torgo.interpreter.InterpreterThread;
import org.tros.torgo.interpreter.InterpreterValue;
import org.tros.torgo.InterpreterVisualization;
import org.tros.torgo.Main;
import org.tros.torgo.interpreter.Scope;
import org.tros.torgo.interpreter.ScopeListener;

/**
 * Allows viewing a call stack w/ variables as code is executed. (Still under
 * development.)
 *
 * @author matta
 */
public class StackView implements InterpreterVisualization {

    public static final int DEFAULT_WIDTH = 640;
    public static final int DEFAULT_HEIGHT = 480;

    private boolean isFinished;
    private InterpreterThread interpreter;
    private static final org.tros.utils.logging.Logger logger = org.tros.utils.logging.Logging.getLogFactory().getLogger(StackView.class);

    private NamedWindow window;

    /**
     * Constructor.
     *
     */
    public StackView() {
    }

    @Override
    public InterpreterVisualization create() {
        return new StackView();
    }

    @Override
    public String getName() {
        return StackView.class.getSimpleName();
    }

    @Override
    public void watch(String name, Controller controller, InterpreterThread interpreter) {
        this.interpreter = interpreter;
        this.interpreter.addInterpreterListener(new InterpreterListener() {

            @Override
            public void started() {
                window.setVisible(true);
            }

            @Override
            public void finished() {
                isFinished = true;
            }

            @Override
            public void error(Exception e) {
            }

            @Override
            public void message(String msg) {
            }

            /**
             * This is where the bulk of the code will go.
             *
             * @param block
             * @param scope
             */
            @Override
            public void currStatement(CodeBlock block, Scope scope) {
                logger.verbose(MessageFormat.format("Curr Statement: {0}", new Object[]{block.getParserRuleContext().getClass().getName()}));
            }
        });

        window = new NamedWindow(name + "-" + this.getClass().getSimpleName(), DEFAULT_WIDTH, DEFAULT_HEIGHT);
        window.setTitle(controller.getLang() + " - Stack View");
        Main.loadIcon(window);

        this.interpreter.addScopeListener(new ScopeListener() {

            @Override
            public void scopePopped(Scope scope, CodeBlock block) {
                logger.verbose(MessageFormat.format("Scope Popped: {0}", new Object[]{block.getClass().getName()}));
            }

            @Override
            public void scopePushed(Scope scope, CodeBlock block) {
                logger.verbose(MessageFormat.format("Scope Pushed: {0}", new Object[]{block.getClass().getName()}));
            }

            @Override
            public void variableSet(Scope scope, String name, InterpreterValue value) {
                if (!name.contains("%")) {
                    logger.verbose(MessageFormat.format("Variable Set: {0} -> {1}", new Object[]{name, value.toString()}));
                }
            }
        });

        window.setDefaultCloseOperation(HIDE_ON_CLOSE);
        isFinished = false;

        window.addWindowListener(new WindowListener() {

            @Override
            public void windowOpened(WindowEvent we) {
            }

            @Override
            public void windowClosing(WindowEvent we) {
                if (isFinished) {
                    window.dispose();
                }
            }

            @Override
            public void windowClosed(WindowEvent we) {
            }

            @Override
            public void windowIconified(WindowEvent we) {
            }

            @Override
            public void windowDeiconified(WindowEvent we) {
            }

            @Override
            public void windowActivated(WindowEvent we) {
            }

            @Override
            public void windowDeactivated(WindowEvent we) {
            }
        });
    }
}
