package jsonform;

public class InfoPktErrorForm implements InfoForm{

	public int error;

	public int getError() {
		return error;
	}

	public void setError(int error) {
		this.error = error;
	}

	@Override
	public void saveData() {
		// TODO Auto-generated method stub
		System.out.println("Error");
	}

	@Override
	public byte[] getData() {
		// TODO Auto-generated method stub
		return null;
	}
	

}