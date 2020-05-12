package org.imcl.platform.tabs;

import kotlin.Pair;
import org.imcl.launch.LauncherScene;
import org.imcl.platform.function.IMCLPage;

public class LeftList {
    public static void addPage(String name, IMCLPage page) {
        LauncherScene.getPages().add(new Pair(name, page));
    }
}
