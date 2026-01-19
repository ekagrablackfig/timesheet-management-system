import pandas as pd
import os

FILE_PATH = "Timesheet Management System.xlsx"
OUTPUT_PATH = "/Users/ekagra/.gemini/antigravity/brain/0926c0a6-59b3-4ee4-a3f9-0303577a3730/requirements.md"

def extract_requirements():
    if not os.path.exists(FILE_PATH):
        print(f"Error: {FILE_PATH} not found.")
        return

    md_content = "# Timesheet Management System Requirements\n\n"
    
    xl = pd.ExcelFile(FILE_PATH)
    
    # 1. Actors
    if 'Actors' in xl.sheet_names:
        md_content += "## 1. Actors\n\n"
        df = pd.read_excel(FILE_PATH, sheet_name='Actors')
        # Simple heuristic: Look for rows with content in multiple columns, skipping header junk
        # Based on inspection, data seemed to be around column index 3 (Description) and 4 (Constraints?)
        # Let's clean it up: drop completely empty rows
        df_clean = df.dropna(how='all')
        for index, row in df_clean.iterrows():
            # Convert row to string to search for keywords or content
            row_str = [str(x) for x in row if pd.notna(x)]
            if len(row_str) > 1: # Skip rows with too little info
                 md_content += f"- {', '.join(row_str)}\n"
        md_content += "\n"

    # 2. High-Level Use Cases
    if 'HIGH-LEVEL USE CASES' in xl.sheet_names:
        md_content += "## 2. High-Level Use Cases\n\n"
        df = pd.read_excel(FILE_PATH, sheet_name='HIGH-LEVEL USE CASES')
        df_clean = df.dropna(how='all')
        for index, row in df_clean.iterrows():
             row_str = [str(x) for x in row if pd.notna(x)]
             if len(row_str) > 1:
                md_content += f"- {', '.join(row_str)}\n"
        md_content += "\n"

    # 3. Functional Requirements
    if 'FUNCTIONAL REQ' in xl.sheet_names:
        md_content += "## 3. Functional Requirements\n\n"
        df = pd.read_excel(FILE_PATH, sheet_name='FUNCTIONAL REQ')
        df_clean = df.dropna(how='all')
        for index, row in df_clean.iterrows():
             row_str = [str(x) for x in row if pd.notna(x)]
             if len(row_str) > 1:
                md_content += f"- {', '.join(row_str)}\n"
        md_content += "\n"
        
    # 4. Non-Functional Requirements
    if 'NON-FUNC REQ' in xl.sheet_names:
        md_content += "## 4. Non-Functional Requirements\n\n"
        df = pd.read_excel(FILE_PATH, sheet_name='NON-FUNC REQ')
        df_clean = df.dropna(how='all')
        for index, row in df_clean.iterrows():
             row_str = [str(x) for x in row if pd.notna(x)]
             if len(row_str) > 1:
                md_content += f"- {', '.join(row_str)}\n"
        md_content += "\n"

    # 5. Tools
    if 'TOOLS' in xl.sheet_names:
        md_content += "## 5. Technology Stack (Tools)\n\n"
        df = pd.read_excel(FILE_PATH, sheet_name='TOOLS')
        df_clean = df.dropna(how='all')
        for index, row in df_clean.iterrows():
             row_str = [str(x) for x in row if pd.notna(x)]
             if len(row_str) >= 1:
                md_content += f"- {', '.join(row_str)}\n"
        md_content += "\n"

    with open(OUTPUT_PATH, "w") as f:
        f.write(md_content)
    
    print(f"Requirements extracted to {OUTPUT_PATH}")

if __name__ == "__main__":
    extract_requirements()
