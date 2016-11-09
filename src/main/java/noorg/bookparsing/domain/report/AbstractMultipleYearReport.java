package noorg.bookparsing.domain.report;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import noorg.bookparsing.domain.Book;
import noorg.bookparsing.report.format.BookFormatter;

public abstract class AbstractMultipleYearReport extends AbstractReport{
	public static final int UNKNOWN_YEAR = Integer.MIN_VALUE;

	protected HashMap<Integer, Set<Book>> booksByYear = new HashMap<>();
	
	@Override
	protected boolean shouldAddBook(Book book) {
		boolean addBook = false;
		
		// only add books that have been read
		Set<Integer> yearsRead = book.getYearsRead();
		if((yearsRead != null) && (yearsRead.size()>0)){
			addBook = true;
		}
		
		return addBook;
	}

	@Override
	protected void processAddedBook(Book book) {
		Set<Integer> years = book.getYearsRead();
		
		// if added, count by year
		if((years != null) && (!years.isEmpty())){
			for(Integer bookYear: years){
				incrementYear(bookYear, book);
			}
		}else{
			incrementYear(UNKNOWN_YEAR, book);
		}
		
	}
	
	private void incrementYear(final Integer bookYear, final Book book){
		int yearKey;
		
		if(bookYear != null){
			yearKey = bookYear;
		}else{
			yearKey = UNKNOWN_YEAR;
		}
		
		Set<Book> yearBooks = booksByYear.get(yearKey);
		if(yearBooks == null){
			yearBooks = new HashSet<>();
			booksByYear.put(bookYear, yearBooks);
		}
		
		yearBooks.add(book);
	}
	
	@Override
	public String getReport(BookFormatter formatter) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("\n\n").append(getReportHeader()).append("\n");
		sb.append("Total Books: ").append(getTotal()).append("\n");
		sb.append("Year Breakdown:\n");
		
		SortedSet<Integer> yearsSorted = new TreeSet<Integer>(booksByYear.keySet());
		
		for(Integer year: yearsSorted){
			Set<Book> yearBooks = booksByYear.get(year);
			
			// TODO null check on year?
			if(yearBooks != null){
				if(UNKNOWN_YEAR == year){
					sb.append("\nNone: ");
				}else{
					sb.append("\n").append(year).append(": ");
				}
				
				sb.append(yearBooks.size());
				sb.append("\n\n");
				sb.append(formatBookList(yearBooks, formatter));
			}
		}
		
		return sb.toString();
	}
	
	protected abstract String getReportHeader();
	
}
