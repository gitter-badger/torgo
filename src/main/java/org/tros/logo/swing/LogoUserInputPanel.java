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
package org.tros.logo.swing;

import org.tros.torgo.swing.Localization;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import org.tros.torgo.interpreter.CodeBlock;
import org.tros.torgo.Controller;
import org.tros.torgo.interpreter.InterpreterListener;
import org.tros.torgo.interpreter.Scope;
import org.tros.torgo.TorgoTextConsole;

public final class LogoUserInputPanel extends JPanel implements TorgoTextConsole {

    protected final JTextArea inputTextArea;
    private final JTextArea outputTextArea;
    private final JSplitPane splitPane;
    private final JPanel inputTab;
    private final JTabbedPane tabs;

    /**
     * Constructor.
     *
     * @param controller
     */
    public LogoUserInputPanel(Controller controller) {
        BorderLayout layout = new BorderLayout();
        setLayout(layout);

        //SOURCE
        inputTab = new JPanel();
        BorderLayout intputLayout = new BorderLayout();
        inputTab.setLayout(intputLayout);

        JTextArea area = null;
        JScrollPane inputScrollPane = null;
        try {
            Class<?> syntax = Class.forName("org.fife.ui.rsyntaxtextarea.RSyntaxTextArea");
            area = (JTextArea) syntax.newInstance();
            syntax.getMethod("setAntiAliasingEnabled", boolean.class).invoke(area, true);
            syntax.getMethod("setCodeFoldingEnabled", boolean.class).invoke(area, true);
            Class<?> syntaxScroll = Class.forName("org.fife.ui.rtextarea.RTextScrollPane");
            inputScrollPane = (JScrollPane) syntaxScroll.getConstructor(Component.class).newInstance(area);
        } catch (ClassNotFoundException ex) {
            org.tros.utils.logging.Logging.getLogFactory().getLogger(LogoUserInputPanel.class).fatal(null, ex);
        } catch (InstantiationException ex) {
            org.tros.utils.logging.Logging.getLogFactory().getLogger(LogoUserInputPanel.class).fatal(null, ex);
        } catch (IllegalAccessException ex) {
            org.tros.utils.logging.Logging.getLogFactory().getLogger(LogoUserInputPanel.class).fatal(null, ex);
        } catch (NoSuchMethodException ex) {
            org.tros.utils.logging.Logging.getLogFactory().getLogger(LogoUserInputPanel.class).fatal(null, ex);
        } catch (SecurityException ex) {
            org.tros.utils.logging.Logging.getLogFactory().getLogger(LogoUserInputPanel.class).fatal(null, ex);
        } catch (IllegalArgumentException ex) {
            org.tros.utils.logging.Logging.getLogFactory().getLogger(LogoUserInputPanel.class).fatal(null, ex);
        } catch (InvocationTargetException ex) {
            org.tros.utils.logging.Logging.getLogFactory().getLogger(LogoUserInputPanel.class).fatal(null, ex);
        }
        inputTextArea = area == null ? new JTextArea() : area;
        inputScrollPane = inputScrollPane == null ? new JScrollPane(inputTextArea) : inputScrollPane;

        inputTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        inputTab.add(inputScrollPane, BorderLayout.CENTER);

        //TABS
        tabs = new JTabbedPane();
        tabs.add("Logo", inputTab);

        JPanel output = new JPanel();
        BorderLayout outputLayout = new BorderLayout();
        output.setLayout(outputLayout);

        outputTextArea = new JTextArea();
        outputTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        outputTextArea.setEditable(false);
        JScrollPane outputScrollPane = new JScrollPane(outputTextArea);

        JLabel messagesLabel = new JLabel(Localization.getLocalizedString("MessagesLabel"));
        JPanel messages = new JPanel();
        messages.setLayout(new BorderLayout());
        messages.add(messagesLabel, BorderLayout.PAGE_START);
        messages.add(outputScrollPane, BorderLayout.CENTER);
        output.add(messages, BorderLayout.CENTER);

        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tabs, output);
        final java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(LogoUserInputPanel.class);
        int dividerLocation = prefs.getInt(LogoUserInputPanel.class.getName() + "divider-location", 0);
        dividerLocation = dividerLocation == 0 ? 400 : dividerLocation;
        splitPane.setDividerLocation(dividerLocation);
        splitPane.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent pce) {
                prefs.putInt(LogoUserInputPanel.class.getName() + "divider-location", splitPane.getDividerLocation());
            }
        });
        this.add(splitPane);

        controller.addInterpreterListener(new InterpreterListener() {

            /**
             * Clears the highlighted areas.
             */
            @Override
            public void started() {
                Highlighter hl = inputTextArea.getHighlighter();
                inputTextArea.setEditable(false);
                hl.removeAllHighlights();
            }

            /**
             * Clears the highlighted areas.
             */
            @Override
            public void finished() {
                Highlighter hl = inputTextArea.getHighlighter();
                inputTextArea.setEditable(true);
                hl.removeAllHighlights();
            }

            /**
             * Clears the highlighted areas.
             */
            @Override
            public void error(Exception e) {
                Highlighter hl = inputTextArea.getHighlighter();
                inputTextArea.setEditable(true);
                hl.removeAllHighlights();
            }

            /**
             * Append a message to the output area.
             *
             * @param msg
             */
            @Override
            public void message(String msg) {
                appendToOutputTextArea(msg);
            }

            @Override
            public void currStatement(CodeBlock block, Scope scope) {
            }
        });
    }

    /**
     * Reset the control to initial state.
     */
    @Override
    public void reset() {
        clearSource();
        clearOutputTextArea();
    }

    /**
     * Clear the output text area.
     */
    @Override
    public void clearOutputTextArea() {
        outputTextArea.setText("");
    }

    /**
     * Append text to the output text area.
     *
     * @param what
     */
    @Override
    public void appendToOutputTextArea(String what) {
        outputTextArea.append(what);
    }

    /**
     * Get the source to interpret.
     *
     * @return
     */
    @Override
    public String getSource() {
        return (inputTextArea.getText());
    }

    /**
     * Set the source to interpret.
     *
     * @param source
     */
    @Override
    public void setSource(String source) {
        source = source.replace("\r", "");
        inputTextArea.setText(source);
    }

    /**
     * Append a string to the source.
     *
     * @param source
     */
    @Override
    public void appendToSource(String source) {
        if (inputTextArea.isEditable()) {
            inputTextArea.setText(inputTextArea.getText() + System.getProperty("line.separator") + source);
        }
    }

    /**
     * Insert a string into the source at the cursor.
     *
     * @param source
     */
    @Override
    public void insertIntoSource(String source) {
        if (inputTextArea.isEditable()) {
            int c = 0;
            boolean success = false;
            try {
                if (inputTextArea.getClass().getMethod("getCaretOffsetFromLineStart") != null) {
                    c = ((Integer) inputTextArea.getClass().getMethod("getCaretOffsetFromLineStart").invoke(inputTextArea));
                    success = true;
                }
            } catch (NoSuchMethodException ex) {
                org.tros.utils.logging.Logging.getLogFactory().getLogger(LogoUserInputPanel.class).fatal(null, ex);
            } catch (SecurityException ex) {
                org.tros.utils.logging.Logging.getLogFactory().getLogger(LogoUserInputPanel.class).fatal(null, ex);
            } catch (IllegalAccessException ex) {
                org.tros.utils.logging.Logging.getLogFactory().getLogger(LogoUserInputPanel.class).fatal(null, ex);
            } catch (IllegalArgumentException ex) {
                org.tros.utils.logging.Logging.getLogFactory().getLogger(LogoUserInputPanel.class).fatal(null, ex);
            } catch (InvocationTargetException ex) {
                org.tros.utils.logging.Logging.getLogFactory().getLogger(LogoUserInputPanel.class).fatal(null, ex);
            }

            if (success) {
                if (c != 0) {
                    source = System.getProperty("line.separator") + source;
                }
                inputTextArea.insert(source, inputTextArea.getCaretPosition());
            } else {
                appendToSource(source);
            }
        }
    }

    /**
     * Clear the source.
     */
    @Override
    public void clearSource() {
        if (inputTextArea.isEditable()) {
            setSource("");
        }
    }

    /**
     * To to a position in the source.
     *
     * @param position
     */
    @Override
    public void gotoPosition(int position) {
        inputTextArea.setCaretPosition(position);
    }

    /**
     * Highlight a section of the source.
     *
     * @param line
     * @param startChar
     * @param endChar
     */
    @Override
    public void highlight(int line, int startChar, int endChar) {
        if (line > 0) {
            Highlighter hl = inputTextArea.getHighlighter();
            hl.removeAllHighlights();
            try {
                hl.addHighlight(startChar, endChar + 1, DefaultHighlighter.DefaultPainter);
            } catch (BadLocationException ex) {
                org.tros.utils.logging.Logging.getLogFactory().getLogger(LogoUserInputPanel.class).fatal(null, ex);
            }
        }
    }

    /**
     * Get the swing component of the object.
     *
     * @return
     */
    @Override
    public Component getComponent() {
        return this;
    }
}
