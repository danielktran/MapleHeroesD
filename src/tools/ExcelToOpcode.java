package tools;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.Iterator;
import java.util.Stack;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelToOpcode {
	
	public static void main(String[] args) {
		try {
            FileInputStream file = new FileInputStream(new File("gMS Opcodes.xlsx"));
 
            //Create Workbook instance holding reference to .xlsx file
            XSSFWorkbook workbook = new XSSFWorkbook(file);
 
            for (int i = 0; i < 2; i++) {
	            XSSFSheet sheet = workbook.getSheetAt(i);
	            FileWriter out = new FileWriter(new File(sheet.getSheetName() + ".java"));
	            out.write("package net;\n\n");
	            out.write("public enum " + sheet.getSheetName() + " {\n\n"); // Write class header
	            
	 
	            //Iterate through each rows one by one
	            Iterator<Row> rowIterator = sheet.iterator();
	            while (rowIterator.hasNext()) {
	                Row row = rowIterator.next();
	                //For each row, iterate through all the columns
	                Iterator<Cell> cellIterator = row.cellIterator();
	                Stack<String> opcodes = new Stack<String>();
	                
	                Cell cell = cellIterator.next();
	                if (!cell.getStringCellValue().isEmpty()) {
		                if (cell.getStringCellValue().equals("Opcode Name")) { // Skip first line
		                	continue;
		                } else {
		                	out.write("\t" + cell.getStringCellValue() + "((short) 0x"); // Write opcode header name
		                }
	                }
	                
	                while (cellIterator.hasNext()) 
	                {
	                    cell = cellIterator.next();
	                    if (!cell.getStringCellValue().isEmpty()) { 
	                    	opcodes.push(cell.getStringCellValue()); // Add opcode to stack if its not empty
	                    }
	                    
	                }
	                if (!opcodes.isEmpty()) { // Get the latest opcode
	                	String opcode = opcodes.pop();
	                	if (opcode.length() == 1) {
	                		opcode = "0" + opcode;
	                	}
	                	out.write(opcode + ")" + (rowIterator.hasNext() ? "," : ";") + "\n");
	                }
	            }
	            
	            // Print opcode methods
	            out.write("\n\tprivate short opcode;\n\n");
	            out.write("\tprivate " + sheet.getSheetName() + "(short opcode) {\n");
	            out.write("\t\tthis.opcode = opcode;\n");
	            out.write("\t}\n\n");
	            
	            out.write("\tpublic short getOpcode() {\n");
	            out.write("\t\treturn opcode;\n");
	            out.write("\t}\n\n");
	            
	            out.write("\tpublic void setOpcode(short opcode) {\n");
	            out.write("\t\tthis.opcode = opcode;\n");
	            out.write("\t}\n\n");

	            out.write("}");
	            out.close();
            }
            file.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
		System.out.println("Done exporting opcodes from excel!");
	}

}
