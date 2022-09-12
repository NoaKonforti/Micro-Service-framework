package bgu.spl.mics.application.objects;

import java.util.Comparator;

public class CPUCompare implements Comparator<CPU> {


        // override the compare() method
        public int compare(CPU c1, CPU c2)
        {
            if (c1.getCores() == c2.getCores())
                return 0;
            else if (c1.getCores() > c2.getCores())
                return -1;
            else
                return 1;
        }
    }

