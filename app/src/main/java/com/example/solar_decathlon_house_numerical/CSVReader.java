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
            reader.readLine();

            if(power_water.equals("power")){
                reader.readLine();
                reader.readLine();
            }

            //Read the .csv file one line at a time all the way till we reach the end.
            while((csvLine = reader.readLine()) != null){
                double summation = 0;   /* Summation is used to create an extra column at the end of the line.
                                         * It stores the sum of all the sensor values for that timestamp.
                                         * It is used for the Summed-Up Power Consumption Graph and for Water Consumption.*/

                String[] row = csvLine.split(",");  //Splits the row into array of sensor values.

                //For loop for summation of the split water sensor values.
                if(power_water.equals("water")){
                    for(int i = 1; i < row.length; i++){
                        summation += Double.parseDouble(row[i]); //Convert .csv String values to double for summation.
                    }

                    String newStringWater = Double.toString(summation);
                    String[] newRowWater = new String[row.length + 1];
                    resultList.add(waterPower(newStringWater, newRowWater, row)); //Call a function "waterPower" to do the work
                }

                //For loop for summation of the split power consumption sensor values.
                if(power_water.equals("power")){
                    for(int i = 2; i < row.length-1; i++){
                        summation += Double.parseDouble(row[i]); //Convert .csv String values to double for summation.
                    }

                    String newStringPower = Double.toString(summation);
                    String[] newRowPower = new String[row.length + 1];
                    resultList.add(waterPower(newStringPower, newRowPower, row)); //Call a function "waterPower" to do the work
                }

                else{
                    //Does Nothing extra. No row is added
                    //This is the case when we are working with Temperature.
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

    private static String[] waterPower(String newString, String[] newRow, String[] row)
    {
        //For loop to add extra row at the end of the row array.
        for(int i = 0; i < row.length; i++){
            newRow[i] = row[i];
            if(i+1 == row.length){
                newRow[i+1] = newString;
            }
        }
        return newRow;
    }
}