package it.unibo.oop.reactivegui03;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unibo.oop.JFrameUtil;

import java.io.Serial;
import java.lang.reflect.InvocationTargetException;

/**
 * Third experiment with reactive gui.
 */
public final class AnotherConcurrentGUI extends JFrame {

    @Serial
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(AnotherConcurrentGUI.class);
    private static final int STOP = 10_000;
    private final JLabel display = new JLabel();

    /**
     * Builds a new CGUI.
     */
    public AnotherConcurrentGUI() {
        super();
        JFrameUtil.dimensionJFrame(this);
        final JPanel panel = new JPanel();
        panel.add(display);
        final JButton stop = new JButton("stop");
        final JButton up = new JButton("up");
        final JButton down = new JButton("down");
        panel.add(up);
        panel.add(down);
        panel.add(stop);
        this.getContentPane().add(panel);
        this.setVisible(true);
        final Agent agent = new Agent();
        new Thread(agent).start();
        stop.addActionListener(e -> {
            agent.stopCounting();
            stop.setEnabled(false);
            up.setEnabled(false);
            down.setEnabled(false);
        });
        up.addActionListener(e -> agent.flag = false);
        down.addActionListener(e -> agent.flag = true);

        new Thread(() -> {
            try {
                Thread.sleep(STOP);
            } catch (final InterruptedException e) {
                e.printStackTrace(); //NOPMD
            }
            agent.stopCounting();
        }).start();
    }

    private final class Agent implements Runnable {
        private volatile boolean stop;
        private volatile boolean flag;
        private int counter;

        @Override
        public void run() {
            while (!this.stop) {
                if (!flag) {
                try {
                    // The EDT doesn't access `counter` anymore, it doesn't need to be volatile
                    final var nextText = Integer.toString(this.counter);
                    SwingUtilities.invokeAndWait(() -> AnotherConcurrentGUI.this.display.setText(nextText));
                    this.counter++;
                    Thread.sleep(100);
                } catch (InvocationTargetException | InterruptedException ex) {
                    LOGGER.error(ex.getMessage(), ex);
                }
            } else {
                try {
                    // The EDT doesn't access `counter` anymore, it doesn't need to be volatile
                    final var nextText = Integer.toString(this.counter);
                    SwingUtilities.invokeAndWait(() -> AnotherConcurrentGUI.this.display.setText(nextText));
                    this.counter--;
                    Thread.sleep(100);
                } catch (InvocationTargetException | InterruptedException ex) {
                    LOGGER.error(ex.getMessage(), ex);
                    }
                }
            }
        }

        public void stopCounting() {
            this.stop = true;
        }
    }
}
