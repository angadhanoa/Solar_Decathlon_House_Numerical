package com.example.solar_decathlon_house_numerical;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CSVReader {
    InputStream inputStream;
    String power_water;

    public CSVReader(InputStream is, String pw){
        this.inputStream = is;
        this.power_water = pw;
    }

    public List<String[]> read(){
        List<String[]> resultList = new ArrayList<String[]>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        //Try to populate, if successful then try to close the file.
        try{
            String csvLine;
            for(int i = 0; i < 15; i++) {
                reader.readLine();  //First 15 rows are about initializing files, skip it.
            }

            //Read the .csv file one line at a time all the way till we reach the end.
            while((csvLine = reader.readLine()) != null){
                String[] row;

                if(power_water.equalsIgnoreCase("water")) {
                    row = csvLine.split(",");  //Splits the row into array of sensor values.
                    resultList.add(row);
                }
                else{
                    row = csvLine.split("\t");  //This should imply the tab character to split the row.
                    resultList.add(row);
                }
            }
        }
        //Try to populate, if fail then catch the error and inform the user.
        catch(IOException ex){
            throw new RuntimeException("Error in Reading CSV File:" + ex);
        }

        //After the first part is done, Try to close the input Stream.
        finally{
            //Try to close the input stream, if possible go ahead
            try{
                inputStream.close();
            }
            //Otherwise catch the error and inform the user.
            catch(IOException e){
                throw new RuntimeException("Error in closing Input Stream:" + e);
            }
        }
        return resultList;
    }
}