package com.jsuereth.video.swing;

import com.jsuereth.video.filters.FiltersRegistry;

import javax.swing.*;
import java.awt.*;

/**
 * TODO: javadoc
 * Created by Michael Ilyin on 17.01.2017.
 */
class CellRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        setText(((FiltersRegistry.FilterMeta)value).name());
        return this;
    }
}
