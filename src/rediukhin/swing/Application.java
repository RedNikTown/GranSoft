package rediukhin.swing;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static javax.swing.JFrame.EXIT_ON_CLOSE;

public class Application {
    private static final int WIDTH = 600;
    private static final int HEIGHT = 600;
    private static final int BUTTONS_IN_COLUMN = 10;
    private static final int MILLIS_BETWEEN_ITERATION = 222;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(IntroScreen::new);
    }

    static class IntroScreen {

        IntroScreen() {
            JFrame frame = new JFrame("GranSoft");
            frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
            frame.setVisible(true);
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Dimension dimension = toolkit.getScreenSize();
            frame.setBounds(dimension.width / 2 - WIDTH / 2, dimension.height / 2 - HEIGHT / 2, WIDTH, HEIGHT);
            createForm(frame);
        }

        public static void createForm(JFrame frame) {
            Container container = frame.getContentPane();
            container.removeAll();
            JPanel gridBagPanel = new JPanel();
            gridBagPanel.setLayout(new GridBagLayout());
            Label label = new Label("How many numbers to display?");
            JTextField field = new JTextField(10);
            field.setFont(new Font("Dialog", Font.PLAIN, 14));
            CustomButton jButton = new CustomButton("Enter", new Color(36, 120, 247));
            jButton.setPreferredSize(new Dimension(110, 25));
            jButton.addActionListener(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String input = field.getText();
                    try {
                        int number = Integer.parseInt(input.trim());
                        if (number > 0 && number <= 1000) {
                            List<JButton> jButtonList = new ButtonCreator().createButtons(frame, number);
                            SwingUtilities.invokeLater(() -> new SortScreen(frame, jButtonList));
                        } else throw new NumberFormatException();
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(gridBagPanel, "Invalid input", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(5, 3, 5, 3);
            c.gridy = 0;
            gridBagPanel.add(label, c);
            c.gridy = 1;
            gridBagPanel.add(field, c);
            c.gridy = 2;
            gridBagPanel.add(jButton, c);
            container.add(gridBagPanel, BorderLayout.CENTER);
            container.revalidate();
        }
    }

    static class SortScreen {

        List<JButton> jButtonList;
        JFrame frame;
        JPanel numbersPanel;
        JPanel controlButtonsPanel;
        boolean sorted;
        ExecutorService executorService;

        public SortScreen(JFrame frame, List<JButton> jButtonList) {
            this.jButtonList = jButtonList;
            this.frame = frame;
            executorService = Executors.newSingleThreadExecutor();

            frame.getContentPane().removeAll();
            numbersPanel = new JPanel();
            numbersPanel.setLayout(new GridBagLayout());
            Border emptyBorder = BorderFactory.createEmptyBorder(10, 10, 10, 10);
            numbersPanel.setBorder(emptyBorder);
            controlButtonsPanel = new JPanel(new GridLayout(2, 1, 3, 3));
            controlButtonsPanel.setBorder(emptyBorder);

            paintButtons(jButtonList, numbersPanel);

            JPanel flowNumber = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JPanel flowControl = new JPanel(new FlowLayout(FlowLayout.RIGHT));

            flowNumber.add(numbersPanel);
            flowControl.add(controlButtonsPanel);

            paintControlButton(jButtonList, controlButtonsPanel);

            frame.add(flowNumber, BorderLayout.WEST);
            frame.add(flowControl, BorderLayout.EAST);
            frame.revalidate();
            frame.repaint();
        }

        private void paintControlButton(List<JButton> jButtonList, JPanel controlButtonsPanel) {
            CustomButton sort = new CustomButton("sort", new Color(0, 247, 170));
            sort.setPreferredSize(new Dimension(80, 30));
            sort.addActionListener(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    SwingUtilities.invokeLater(() -> {
                        executorService.submit(new QuickSort(jButtonList, numbersPanel));
                    });
                }
            });
            controlButtonsPanel.add(sort);
            CustomButton reset = new CustomButton("reset", new Color(0, 247, 170));
            reset.setPreferredSize(new Dimension(80, 30));
            reset.addActionListener(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    executorService.shutdown();
                    SwingUtilities.invokeLater(() -> IntroScreen.createForm(frame));
                }
            });
            controlButtonsPanel.add(reset);
        }

        public void paintButtons(List<JButton> buttons, JPanel panel) {
            GridBagConstraints c = new GridBagConstraints();
            Insets insets = new Insets(3, 3, 3, 3);
            int x = 0, y = 0;
            for (JButton button : buttons) {
                if (y == BUTTONS_IN_COLUMN) {
                    y = 0;
                    x++;
                }
                c.gridy = y++;
                c.gridx = x;
                c.insets = insets;
                panel.add(button, c);
            }
        }

        class QuickSort implements Runnable {

            private JPanel panel;
            private List<JButton> jButtonList;

            public QuickSort(List<JButton> jButtonList, JPanel panel) {
                this.panel = panel;
                this.jButtonList = jButtonList;
            }

            private void quickSortDescending(List<JButton> buttonList, int start, int end) {
                if (buttonList.size() == 0) {
                    return;
                }
                if (start >= end) {
                    return;
                }
                int middle = start + (end - start) / 2;
                int opora = getInt(buttonList.get(middle));
                int i = start, j = end;
                while (i <= j) {
                    if (!sorted) {
                        while (getInt(buttonList.get(i)) < opora) {
                            i++;
                        }
                        while (getInt(buttonList.get(j)) > opora) {
                            j--;
                        }
                    } else {
                        while (getInt(buttonList.get(i)) > opora) {
                            i++;
                        }
                        while (getInt(buttonList.get(j)) < opora) {
                            j--;
                        }
                    }
                    if (i <= j) {
                        JButton temp = buttonList.get(i);
                        buttonList.set(i, buttonList.get(j));
                        buttonList.set(j, temp);
                        i++;
                        j--;
                        refreshSortPage(buttonList, panel);
                    }
                }
                if (start < j) {
                    quickSortDescending(buttonList, start, j);
                }
                if (end > i) {
                    quickSortDescending(buttonList, i, end);
                }
            }

            private void quickSortIncreasing(List<JButton> buttonList, int start, int end) {
                if (buttonList.size() == 0) {
                    return;
                }
                if (start >= end) {
                    return;
                }
                int middle = start + (end - start) / 2;
                int opora = getInt(buttonList.get(middle));
                int i = start, j = end;
                while (i <= j) {
                    while (getInt(buttonList.get(i)) > opora) {
                        i++;
                    }
                    while (getInt(buttonList.get(j)) < opora) {
                        j--;
                    }
                    if (i <= j) {
                        JButton temp = buttonList.get(i);
                        buttonList.set(i, buttonList.get(j));
                        buttonList.set(j, temp);
                        i++;
                        j--;
                        refreshSortPage(buttonList, panel);
                    }
                }
                if (start < j) {
                    quickSortIncreasing(buttonList, start, j);
                }
                if (end > i) {
                    quickSortIncreasing(buttonList, i, end);
                }
            }

            private void refreshSortPage(List<JButton> jButtonList, JPanel panel) {
                panel.removeAll();
                paintButtons(jButtonList, panel);
                panel.revalidate();
                panel.repaint();
                try {
                    Thread.sleep(MILLIS_BETWEEN_ITERATION);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            private int getInt(JButton button) {
                return Integer.parseInt(button.getText());
            }

            @Override
            public void run() {
                if (!sorted) {
                    quickSortDescending(jButtonList, 0, jButtonList.size() - 1);
                    sorted = true;
                } else {
                    quickSortIncreasing(jButtonList, 0, jButtonList.size() - 1);
                    sorted = false;
                }
            }
        }
    }


    static class ButtonCreator {

        public List<JButton> createButtons(JFrame frame, int countOfButton) {
            List<Integer> numbersOfButtons = createNumberForButtons(countOfButton);
            List<JButton> buttons = new ArrayList<>();
            for (int i = 0; i < countOfButton; i++) {
                CustomButton button = new CustomButton(String.valueOf(numbersOfButtons.get(i)), new Color(36, 120, 247));
                button.addActionListener(new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        int numberOfButton = Integer.parseInt(e.getActionCommand());
                        if (numberOfButton <= 30) {
                            List<JButton> newJButtons = new ButtonCreator().createButtons(frame, numberOfButton);
                            SwingUtilities.invokeLater(() -> new SortScreen(frame, newJButtons));
                        } else
                            JOptionPane.showMessageDialog(frame, "Please select a value smaller or equal to 30.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                });
                buttons.add(button);
            }
            return buttons;
        }

        private List<Integer> createNumberForButtons(int countOfButton) {
            List<Integer> list = new ArrayList<>(countOfButton);
            for (int i = 0; i < countOfButton; i++) {
                if (!list.isEmpty()) {
                    list.add(1 + (int) (Math.random() * 1000));
                } else {
                    list.add(1 + (int) (Math.random() * 30));
                }
            }
            Collections.shuffle(list);
            return list;
        }
    }

    static class CustomButton extends JButton {

        public CustomButton(String text, Color color) {
            super(text);
            setBackground(color);
            setForeground(Color.WHITE);
            setPreferredSize(new Dimension(70, 30));
        }
    }
}