package com.mz.jarboot.core.cmd.view;

import com.mz.jarboot.common.AnsiLog;
import com.mz.jarboot.core.cmd.model.DumpClassVO;
import com.mz.jarboot.core.cmd.view.element.Element;
import com.mz.jarboot.core.cmd.view.element.TableElement;
import com.mz.jarboot.core.utils.ClassUtils;
import com.mz.jarboot.common.utils.StringUtils;
import com.mz.jarboot.core.utils.TypeRenderUtils;

import java.util.List;

/**
 * @author majianzheng
 */
public class DumpClassView implements ResultView<com.mz.jarboot.core.cmd.model.DumpClassModel> {

    @Override
    public String render(com.mz.jarboot.core.cmd.model.DumpClassModel result) {
        StringBuilder sb = new StringBuilder();
        if (result.getMatchedClassLoaders() != null) {
            sb.append("Matched classloaders: \n");
            ClassLoaderView.drawClassLoaders(sb, result.getMatchedClassLoaders(), false);
            sb.append(StringUtils.LF);
            return sb.toString();
        }
        if (result.getDumpedClasses() != null) {
            drawDumpedClasses(sb, result.getDumpedClasses());
        } else if (result.getMatchedClasses() != null) {
            Element table = ClassUtils.renderMatchedClasses(result.getMatchedClasses());
            sb.append(table.toHtml()).append(StringUtils.LF);
        }
        return sb.toString();
    }

    private void drawDumpedClasses(StringBuilder process, List<DumpClassVO> list) {
        TableElement table = new TableElement();
        table.row(true, "HASHCODE", "CLASSLOADER", "LOCATION");

        for (DumpClassVO clazz : list) {
            table.row(AnsiLog.red(clazz.getClassLoaderHash()),
                    TypeRenderUtils.drawClassLoader(clazz).toHtml(),
                    AnsiLog.red(clazz.getLocation()));
        }

        process
                .append(table.toHtml())
                .append(StringUtils.EMPTY);
    }
}
