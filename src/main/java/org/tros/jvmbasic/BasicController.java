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
package org.tros.jvmbasic;

import java.io.File;
import javax.swing.JMenuBar;
import javax.swing.JToolBar;
import org.tros.torgo.ControllerBase;
import org.tros.torgo.Controller;
import org.tros.torgo.interpreter.InterpreterThread;
import org.tros.torgo.TorgoScreen;
import org.tros.torgo.TorgoTextConsole;

/**
 *
 * @author matta
 */
public class BasicController extends ControllerBase {

    @Override
    protected TorgoTextConsole createConsole(Controller app) {
        return null;
    }

    @Override
    protected TorgoScreen createCanvas(TorgoTextConsole console) {
        return null;
    }

    @Override
    protected InterpreterThread createInterpreterThread(String source) {
        return null;
    }

    @Override
    protected JToolBar createToolBar() {
        return null;
    }

    @Override
    protected JMenuBar createMenuBar() {
        return null;
    }

    @Override
    public String getLang() {
        return "jvmBasic";
    }

    @Override
    public void openFile(File file) {
    }
}
