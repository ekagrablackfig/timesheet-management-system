import pandas as pd
import os

FILE_PATH = "Timesheet Management System.xlsx"

def inspect_excel():
    if not os.path.exists(FILE_PATH):
        print(f"Error: File '{FILE_PATH}' not found.")
        return

    print(f"=== Inspecting: {FILE_PATH} ===\n")

    try:
        # Load the Excel file
        xl = pd.ExcelFile(FILE_PATH)
        sheet_names = xl.sheet_names
        print(f"Total Sheets: {len(sheet_names)}")
        print(f"Sheet Names: {', '.join(sheet_names)}\n")

        for sheet in sheet_names:
            print(f"-" * 40)
            print(f"Sheet: '{sheet}'")
            
            # Read the sheet
            df = pd.read_excel(FILE_PATH, sheet_name=sheet)
            
            # Dimensions
            rows, cols = df.shape
            print(f"Dimensions: {rows} rows, {cols} columns")
            
            # Columns and Types
            if cols > 0:
                print("\nColumns and Data Types:")
                print(df.dtypes)
                
                print("\nSample Data (First 5 rows):")
                print(df.head())
                
                print("\nNull Values per Column:")
                print(df.isnull().sum())
            else:
                print("\n(Empty Sheet)")
            
            print("\n")

    except Exception as e:
        print(f"An error occurred while reading the Excel file: {e}")

if __name__ == "__main__":
    inspect_excel()
