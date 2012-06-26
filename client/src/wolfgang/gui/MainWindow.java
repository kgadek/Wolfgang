package wolfgang.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.text.Format;
import java.text.SimpleDateFormat;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import wolfgang.data.DataMaster;
import wolfgang.data.mapper.Operation;

public class MainWindow {

	private DataMaster dm;
	
	public MainWindow() throws ClassNotFoundException, NoSuchAlgorithmException, SecurityException, SQLException, IOException {
		super();
		dm = DataMaster.getInstance();
	}

	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					MainWindow mw = new MainWindow();
					mw.createAndShowMainWindow();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public void createAndShowMainWindow() {
		JFrame mainFrame = new JFrame("Wolfgang");
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JTabbedPane mainPane = new JTabbedPane();
		
		JComponent podsumowaniePane = genPodsumowaniePane();
		mainPane.addTab("Podsumowanie", podsumowaniePane);
		
		JComponent operacjePane = genOperacjePane();
		mainPane.addTab("Operacje", operacjePane);
		
		JComponent kategoriePane = genKategoriePane();
		mainPane.addTab("Kategorie", kategoriePane);
		
		mainPane.setSelectedIndex(1);
		mainFrame.add(mainPane);
		
//		mainFrame.pack();
		mainFrame.setSize(700, 500);
		mainFrame.setVisible(true);
	}

	private static JPanel genPodsumowaniePane() {
		return new JPanel(false);
	}

	private static JPanel genKategoriePane() {
		return new JPanel(false);
	}

	@SuppressWarnings("serial")
	public class OperationTable extends JPanel {
		public OperationTable() {
			super(new GridLayout(1, 1));
			
			Format formatter = new SimpleDateFormat("yyyy-MM-dd");
			
			String[] colNames = { "Użytkownik", "Kategoria", "Data", "Bilans", "Saldo" };
			Object[][] data = new Object[dm.getOperations().size()][];
			int i = 0;
			for(Operation o : dm.getOperations())
				data[i++] = new Object[] { o.user.login, o.category.description, formatter.format(o.dateStart), o.balance, "?" };
			
			JTable table = new JTable(data, colNames);
			table.setPreferredScrollableViewportSize(new Dimension(500, 300));
			table.setFillsViewportHeight(true);
			
			
			table.getColumnModel().getColumn(0).setPreferredWidth(100);
			
			table.getColumnModel().getColumn(1).setPreferredWidth(400);
			
			table.getColumnModel().getColumn(2).setPreferredWidth(100);
			DefaultTableCellRenderer centerAlign = new DefaultTableCellRenderer();
			centerAlign.setHorizontalAlignment(JLabel.CENTER);
			table.getColumnModel().getColumn(2).setCellRenderer(centerAlign);
			
			table.getColumnModel().getColumn(3).setPreferredWidth(80);
			DefaultTableCellRenderer rightAlign = new DefaultTableCellRenderer();
			rightAlign.setHorizontalAlignment(JLabel.RIGHT);
			table.getColumnModel().getColumn(3).setCellRenderer(rightAlign);
			
			table.getColumnModel().getColumn(4).setPreferredWidth(80);
			table.getColumnModel().getColumn(4).setCellRenderer(rightAlign);
			
			add(new JScrollPane(table));
		}
		
	}
	
	private JPanel genOperacjePane() {
		JPanel ret = new JPanel(false);
		GridBagLayout layout = new GridBagLayout();
		ret.setLayout(layout);
		
		GridBagConstraints c = new GridBagConstraints();
		JButton test;
		
		OperationTable table = new OperationTable();
		c.gridy = 0;
		c.gridx = 0;
		c.weightx = 200;
		c.weighty = 100;
		c.fill = GridBagConstraints.BOTH;
		ret.add(table,c);
		
		test = new JButton("lol01");
		c.weightx = 50;
		c.weighty = 50;
		c.gridy = 0;
		c.gridx = 1;
		c.fill = GridBagConstraints.NORTH;
		c.anchor = GridBagConstraints.PAGE_START;
		ret.add(test,c);
		
		test = new JButton("lol10");
		c.gridy = 1;
		c.gridx = 0;
		c.fill = GridBagConstraints.NORTHWEST;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		ret.add(test,c);
		
		test = new JButton("lol11");
		c.gridy = 1;
		c.gridx = 1;
		c.fill = GridBagConstraints.NORTHEAST;
		c.anchor = GridBagConstraints.LAST_LINE_END;
		ret.add(test,c);
		
		return ret;
	}

}
