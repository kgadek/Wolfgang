package wolfgang.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import wolfgang.data.DataMaster;
import wolfgang.data.mapper.Category;
import wolfgang.data.mapper.Operation;
import wolfgang.data.mapper.User;
import wolfgang.utils.Utils;
import wolfgang.utils.Utils.Function1;
import wolfgang.utils.Utils.Function2;

public class MainWindow {

	private DataMaster dm;
	private User logged;
	public OperationTable tableWithOperations;
	public JLabel lbl_balance;

	public MainWindow() throws ClassNotFoundException, NoSuchAlgorithmException, SecurityException, SQLException, IOException {
		super();
		dm = DataMaster.getInstance();
	}

	private static MainWindow mainWindow;
	private static JFrame loginFrame;
	private static JTextField loginTextField;
	private static JTextField passTextField;

	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override public void run() {
				try {
					mainWindow = new MainWindow();

					loginFrame = new JFrame("Logowanie");
					GridBagLayout gbl = new GridBagLayout();
					GridBagConstraints c = new GridBagConstraints();
					loginFrame.setLayout(gbl);

					c.fill = GridBagConstraints.HORIZONTAL;
					c.weightx = 100;
					c.weighty = 20;

					loginFrame.add(loginTextField = new JTextField("login"), c);
					loginFrame.add(passTextField = new JTextField("pass"), c);

					JButton logMeIn = new JButton();
					logMeIn.setText("Logowanie");
					logMeIn.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							if((mainWindow.logged = mainWindow.verifyUser(loginTextField.getText(), passTextField.getText())) != null) {
								loginFrame.setVisible(false);
								(mainWindow.createMainWindow()).setVisible(true);
							} else
								JOptionPane.showMessageDialog(loginFrame, "Błąd logowania");
						}});
					loginFrame.add(logMeIn, c);

					JButton createUser = new JButton();
					createUser.setText("Nowy");
					createUser.addActionListener(new ActionListener() {
						@Override public void actionPerformed(ActionEvent e) {
							if("".equals(loginTextField.getText()) || "".equals(passTextField.getText()))
								JOptionPane.showMessageDialog(loginFrame, "Nie podano loginu i/lub hasła");
							else {
								try {
									if((mainWindow.logged = mainWindow.createOrGetUser(loginTextField.getText(), passTextField.getText())) == null)
										JOptionPane.showMessageDialog(loginFrame, "Błąd: taki użytkownik już istnieje");
									else {
										loginFrame.setVisible(false);
										(mainWindow.createMainWindow()).setVisible(true);
									}
								} catch (SQLException e1) {
									e1.printStackTrace();
									JOptionPane.showMessageDialog(loginFrame, "Błąd:" + e1.getMessage());
								}}}});
					loginFrame.add(createUser);

					loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					loginFrame.pack();
					loginFrame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}}});
	}

	protected User createOrGetUser(String user, String password) throws SQLException {
		return dm.createOrGetUser(loginTextField.getText(), new Date(), passTextField.getText());
	}

	protected User verifyUser(String user, String password) {
		return dm.checkLogin(user, password);
	}

	public JFrame createMainWindow() {
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

		mainFrame.setSize(700, 500);
		return mainFrame;
	}

	private static JPanel genPodsumowaniePane() {
		return new JPanel(false);
	}

	private static JPanel genKategoriePane() {
		return new JPanel(false);
	}

	@SuppressWarnings("serial")
	public class OperationTable extends JPanel {
		private JTable table;
		private DefaultTableModel tm;

		public void reloadData() {
			String[] colNames = { "Kategoria", "Opis", "Data", "Bilans"};
			final Format formatter = new SimpleDateFormat("yyyy-MM-dd");

			// filter -> sort -> map
			List<Operation> res = (List<Operation>) wolfgang.utils.Utils.filter(dm.getOperations(), new Utils.Predicate<Operation>() {
				@Override public boolean apply(Operation type) {
					return type.user.id == logged.id;
				}});
			Collections.sort(res, new Comparator<Operation>() {
				@Override public int compare(Operation o1, Operation o2) {
					if(o1.dateStart == null || o2.dateStart == null) return -1;
					return (int) (o2.dateStart.getTime() - o1.dateStart.getTime());
				}});
			Collection<Object[]> data = Utils.map2(new Function1<Operation, Object[]>() {
				@Override public Object[] apply(Operation o) {
					return new Object[] { o.category.description, o.description, o.dateStart==null?"?":formatter.format(o.dateStart), ((float)o.balance)/100.0};
				}}, res);

			tm.setDataVector(data.toArray(new Object[res.size()][]), colNames);

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

			Integer sum = Utils.reduce(new Function2<Integer, Operation, Integer>() {
				@Override public Integer apply(Integer x, Operation y) {
					return x + y.balance;
				}}, new Integer(0), dm.getOperations());
			lbl_balance.setText(String.valueOf(((float)(sum)) / 100.0)+"\n");


			// ASCII ART!!
			final int toTake = 5;
			final int scale = 5;
			Collection<Operation> recent = Utils.reverse_c(Utils.take(toTake, res));
			Integer maxi = Utils.max(recent, new Function2<Integer, Operation, Integer>() {
				@Override public Integer apply(Integer x, Operation y) {
					if(x == null) return y.balance;
					return y.balance > x ? y.balance : x;
				}});
			Integer mini = Utils.max(recent, new Function2<Integer, Operation, Integer>() {
				@Override public Integer apply(Integer x, Operation y) {
					if(x == null) return y.balance;
					return y.balance < x ? y.balance : x;
				}});
			if(maxi != mini) {
				for(int i=scale-1;i>=0;--i) {
					for(Operation o : recent)
						System.out.print(o.balance*1.0 > ((float)i*(maxi))/(10.0) ? "---" : "   ");
					System.out.println();
				}
				for(@SuppressWarnings("unused") Operation o : recent)
					System.out.print("===");
				System.out.println();
				for(int i=0;i<scale;++i) {
					for(Operation o : recent)
						System.out.print(o.balance*1.0 < ((float)i*(mini))/(10.0) ? "---" : "   ");
					System.out.println();
				}
			}
		}

		public OperationTable() {
			super(new GridLayout(1, 1));

			tm = new DefaultTableModel();
			table = new JTable(tm);
			table.setPreferredScrollableViewportSize(new Dimension(500, 300));
			table.setFillsViewportHeight(true);

			reloadData();

			add(new JScrollPane(table));
		}

	}

	private JFrame genAddOperationPane() {
		final JFrame frame = new JFrame("Wolfgang - dodaj operację");

		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		frame.setLayout(layout);

		JLabel lbl;
		InputVerifier verify_isfloat;
		JButton btn;

		verify_isfloat = new InputVerifier() {
			public boolean verify(JComponent comp) {
				JTextField textField = (JTextField) comp;
				try {
					Float.parseFloat(textField.getText());
					return true;
				} catch (NumberFormatException e) {
					return false;
				}}};

		lbl = new JLabel("opis:");
		c.gridy = 0;
		c.gridx = 0;
		c.weightx = 10;
		c.weighty = 100;
		c.fill = GridBagConstraints.HORIZONTAL;
		frame.add(lbl,c);

		final JTextField dtf = new JTextField("");
		c.gridx = 1;
		c.weightx = 200;
		frame.add(dtf,c); 

		lbl = new JLabel("balans:");
		c.gridy = 1;
		c.gridx = 0;
		c.weightx = 0;
		frame.add(lbl,c);

		final JTextField tf = new JTextField("");
		tf.setInputVerifier(verify_isfloat);
		c.gridy = 1;
		c.gridx = 1;
		c.weightx = 200;
		frame.add(tf,c);

		lbl = new JLabel("kategoria:");
		c.gridy = 2;
		c.gridx = 0;
		c.weightx = 0;
		frame.add(lbl,c);


		Collection<String> a = Utils.map2(new Function1<Category, String>() {
			@Override public String apply(Category x) {
				return String.valueOf( x.description );
			}}, dm.getCategories());

		final JComboBox<String> cmbo_str = new JComboBox<String>(a.toArray(new String[] {}));
		cmbo_str.setEditable(false);
		c.gridx = 1;
		c.weightx = 200;
		frame.add(cmbo_str,c);

		lbl = new JLabel("data:");
		c.gridy = 3;
		c.gridx = 0;
		c.weightx = 0;
		frame.add(lbl,c);

		final JTextField dt = new JTextField(DateFormat.getDateInstance(DateFormat.MEDIUM).format(new Date()));
		c.gridx = 1;
		c.weightx = 200;
		frame.add(dt,c);

		btn = new JButton("Dodaj operację");
		btn.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				boolean ok = false;
				try {
					DateFormat a = DateFormat.getDateInstance();
					String descr = (String) cmbo_str.getSelectedObjects()[0];				
					Category ctg = dm.getCategoryByName(descr);
					Float flt = Float.parseFloat(tf.getText());
					dm.createOperation(logged, ctg, (int) (flt * 100), (int) (flt * 100)+logged.balance, null, a.parse(dt.getText()), 0, null, 0, descr);
					tableWithOperations.reloadData();
					ok = true;
				} catch (SQLException e1) {
					e1.printStackTrace();
					JOptionPane.showMessageDialog(loginFrame, "Błąd");
				} catch (ParseException e1) {
					e1.printStackTrace();
					JOptionPane.showMessageDialog(loginFrame, "Błąd: zła data");
				}
				if(ok)
					frame.dispose();
			}
		});
		c.gridy = 4;
		c.gridx = 1;
		c.weightx = 200;
		c.fill = GridBagConstraints.LINE_END;
		frame.add(btn, c);

		//frame.setSize(350, 400);
		frame.setMinimumSize(new Dimension(350, 0));
		frame.pack();
		return frame;
	}	

	private JFrame genAddCategoryPane() {
		final JFrame frame = new JFrame("Wolfgang - dodaj kategorię");

		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		frame.setLayout(layout);

		JLabel lbl;
		JButton btn;

		lbl = new JLabel("opis:");
		c.gridy = 0;
		c.gridx = 0;
		c.weightx = 0;
		c.weighty = 100;
		c.fill = GridBagConstraints.HORIZONTAL;
		frame.add(lbl,c);

		final JTextField dtf = new JTextField("");
		c.gridy = 0;
		c.gridx = 1;
		c.weightx = 200;
		c.weighty = 100;
		c.fill = GridBagConstraints.HORIZONTAL;
		frame.add(dtf,c); 


		btn = new JButton("Dodaj kategorię");
		btn.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				try {
					dm.createOrGetCategory(logged, dtf.getText(), 0);
				} catch (SQLException e1) {
					e1.printStackTrace();
					JOptionPane.showMessageDialog(loginFrame, "Błąd");
				}
				frame.dispose();
			}});
		c.gridy = 1;
		c.gridx = 1;
		c.weightx = 200;
		c.weighty = 100;
		c.fill = GridBagConstraints.LINE_END;
		frame.add(btn, c);

		//frame.setSize(350, 400);
		frame.setMinimumSize(new Dimension(350, 0));
		frame.pack();
		return frame;
	}	


	private JPanel genOperacjePane() {
		JPanel ret = new JPanel(false);
		GridBagLayout layout = new GridBagLayout();
		ret.setLayout(layout);

		GridBagConstraints c = new GridBagConstraints();
		JButton button;

		lbl_balance = new JLabel("0.00");

		tableWithOperations = new OperationTable();
		c.gridy = 0;
		c.gridx = 0;
		c.weightx = 200;
		c.weighty = 100;
		c.fill = GridBagConstraints.BOTH;
		ret.add(tableWithOperations,c);

		GridLayout fl = new GridLayout(3,1);
		JPanel inn = new JPanel(false);
		inn.setLayout(fl);

		c.weightx = 50;
		c.weighty = 50;
		c.gridy = 0;
		c.gridx = 1;
		c.fill = GridBagConstraints.NORTH;
		c.anchor = GridBagConstraints.PAGE_START;
		ret.add(inn, c);

		button = new JButton("Dodaj operację");
		button.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				genAddOperationPane().setVisible(true);
			}});
		inn.add(button, 0);


		button = new JButton("Dodaj kategorię");	
		button.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				genAddCategoryPane().setVisible(true);
			}});
		inn.add(button, 1);

		button = new JButton("Usuń operację");
		button.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				JOptionPane pane = new JOptionPane(
						"To be or not to be ?\nThat is the question.\nWhether thy want me to remove thy operation\nOr shall not I touch it - that's a suggestion.");
				Object[] options = new String[] { "To be, leave it sir", "Not to be, dispose thy" };
				pane.setOptions(options);
				JDialog dialog = pane.createDialog(new JFrame(), "Wolfgang - remove thy operation");
				dialog.setVisible(true);
				Object obj = pane.getValue(); 
				int result = -1;
				for (int k = 0; k < options.length; k++)
					if (options[k].equals(obj))
						result = k;
				if(result == 1)
					try {
						ArrayList<Integer> lol = new ArrayList<Integer>(); // lol bo tak trudno o reverse...
						for(int row : tableWithOperations.table.getSelectedRows())
							lol.add(row);
						Collections.reverse(lol);
						for(Integer row : lol)
							dm.removeOperationById(((Operation)dm.getOperations().toArray()[row]).id);
					} catch (SQLException e1) {
						e1.printStackTrace();
					}

				tableWithOperations.reloadData();
			}});
		inn.add(button, 2);

		c.gridy = 2;
		c.gridx = 0;
		c.fill = GridBagConstraints.NORTHWEST;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		ret.add(lbl_balance,c);

		button = new JButton("lol11");
		c.gridy = 2;
		c.gridx = 1;
		c.fill = GridBagConstraints.NORTHEAST;
		c.anchor = GridBagConstraints.LAST_LINE_END;
		ret.add(button,c);

		return ret;
	}

}
