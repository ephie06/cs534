// note that enums naturally implement Comparable! They are comparable in the order given below
public enum Value { 
	
	ZERO(0), ONE(1), TWO(2), THREE(3), FOUR(4), FIVE(5), SIX(6), SEVEN(7), 
	EIGHT(8), NINE(9), TEN(10), ELEVEN(11), TWELVE(12), THIRTEEN(13), FOURTEEN(14);
	
	public final int val;

    private Value(int val) {
        this.val = val;
    }    
	
}

/*
How to compare enums:

Value one = Value.ONE;
Value two = Value.TWO;

// compareTo will be positive if the caller is larger than the parameter
// otherwise it will be negative
if (one.compareTo(two) < 0) {
	System.out.println("this will be printed because one is less than two");
}

*/