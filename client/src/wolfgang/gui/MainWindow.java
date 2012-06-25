package wolfgang.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class MainWindow {

	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				createAndShowMainWindow();
			}
		});
	}

	public static void createAndShowMainWindow() {
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

	public static class OperationTable extends JPanel {
		public OperationTable() {
			super(new GridLayout(1, 1));
			
			Format formatter = new SimpleDateFormat("yyyy-MM-dd");
			
			String[] colNames = { "Kategoria", "Opis", "Data", "Bilans", "Saldo" };
			Object[][] data = {
					{ "Wypłaty", "Muszę oddać część wypłaty bo pomyłka", formatter.format(new Date()), -9000.00f, 0.00f },
					{ "Kredyty", "Biorę kredyt by oddać co nie moje", formatter.format(new Date()), 8000.00f, 9000.00f },
					{ "Zakupy", "Nowy telewizor, yay!", formatter.format(new Date(2012, 04, 02)), -9000.00f, 1000.00f },
					{ "Wypłaty", "Duży przypływ gotówki, yay!", formatter.format(new Date(2012, 04, 01)), 10000.00f, 10000.00f }
			};
			
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
	
	private static JPanel genOperacjePane() {
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
