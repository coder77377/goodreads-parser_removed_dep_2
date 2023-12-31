Goodreads Parser
================

# Overview

A utility to parse the CSV files that can be exported from goodreads to allow for various statistical generation, data transformation, and possible data enrichment in future versions.

## Background

I love to read, and I love to rate, review and discuss books. Good Reads has been an excellent place for me to do that. Good Reads offers some nice statistics and graphs to look at your reading, but it didn't go deep enough for me.

Good Reads does offer an API into their data, but due to security it looked to be a real hassle to gain access to it, even for my own account's data, let alone anyone elses. 

However they do allow you to export your data to CSV. Initially I tried to import the CSV into Google Sheets to try to enrich my data and maybe generate some graphs. I found this tedious and slow.

Since I'm a Java developer by trade, I decided to take a programmatic approach. I developed this application for my own purposes, but I was encouraged by some other folks to share my code in case they wanted to use it and/or contribute to it, so here we are. It is very customized to my own purposes, but I tried to write it in a way that it could be useable for others, or modified quickly/easily to be.

## Usage

The basic steps are this:

1. Export Data From Good Reads to CSV
2. Parse Data into [Book](src/main/java/noorg/bookparsing/domain/Book.java) objects
3. (Optional) Enrich the Data
4. Run 1 or more reports against your books

This is largely all accomplished by a little command line application I created called [BookParsing](src/main/java/noorg/bookparsing/BookParsing.java). 

## Future functionality

There are a lot of things I'd love to add to the application, but since I'm working on my own, I'm a bit limited to how much time I'm putting into it, as well as the limitiations of my abilities. Here are some of the things I'd like to add given time and/or skill:

- **Export Data:** Since the application has the ability to enrinch the data, it may be desireable to export the data back out for use elsewhere (say Google Sheets). As to what format(s) the data should take will depend on what the export will be used for. Currently I have no plans to do this until there is a good use case on my part. It may be desirable to export enriched books and/or reports about books.
- **Cross-reference Data Enrichment:** It might be useful if we can use one or more additional datasources to further enrich our data. For example: [ISBN Database API](http://isbndb.com/api/v2/docs) perhaps. I need to investigate what (if any) additional data can be obtained/added to our books. This looks to require an account, and I'm not sure how I'd incorporate that without requiring the user to include their api key.
- **Database backend:** Right now every time the application runs it must read in, parse and enrich the data. This process is really quick, but it requires all of the enrichment be programatic, since all enrichment is lost as soon as the program completes. Saving the data out to a database (likely a RDBMS such as SQLite or MySQL) would allow any enrichment to be preserved for future runs.
- **GUI Front End:** I hate GUI. I haven't done anything GUI in nearly 8 years, and most of my work was using Java Swing. Some kind of web/java script front end is probably more fashionable at this point, but well out of my skillset. It'd be nice to have a file chooser to load data, mechanisms to manually enrich data: correct mistakes, fill in missing data, etc, and to generate pretty graphs. The data enrichment stuff probably requires the database backend as a pre-requisite, because it would be frustrating to spend a lot of time fixing your data only to lose that work between runs. However a file chooser for the input CSV and the graphing capabilities could be pretty useful with the application as it current exists now. These features would transform the application from something for personal use by me/other Java developers into something that could be run by any good reads user. If you're a GUI developer interested in helping with this please let me know.

## Exporting your data

Through a web browser go to the [Good Reads Import/Export page](https://www.goodreads.com/review/import) and click the Export button at the top right corner. This will (after some period of time) offer you a link to downlad a CSV of your books.

# Classes of Note

As mentioned above, I created a basic command-line application called [BookParsing](src/main/java/noorg/bookparsing/BookParsing.java). It currently is very hard coded to read in the CSV included as part of the project and run whatever reports I was last messing around with. A better main application will be necessary for use by any non Java developer.

## Parsing

This part is very basic. I created an interface [ParsingService](src/main/java/noorg/bookparsing/service/ParsingService.java) which for a given input retuns an output that is a type of [Book](src/main/java/noorg/bookparsing/domain/Book.java). 

The only implementation is the [GoodReadsParsingService](src/main/java/noorg/bookparsing/service/impl/GoodReadsParsingService.java) which is customized CSV parsing based on the known CSV order at the time. If/when that format changes, that impelmentation will need to be updated. This makes the code a bit brittle, but at least it's encapsulated in one place. I documented the expected format which I figured out mostly through trial and error. 

I'm using an open source Java CSV parser called [OpenCSV](http://opencsv.sourceforge.net/).

The parsing also attempts to enrich the data based on how I shelve my data. Ideally this should be moved into the Enrinchment service layer and done post-parsing, but some of the enrichment relies on the raw values from the CSV that would need to be passed to the various enrichers in addition to the book being enriched. More on this below.  

## Enrichment

This is probably the weakest feature so far. It is entirely data specific. In particular it's tied to how I shelve my books on Good Reads. In theory anyone can shelve there data in the same way, but that's a lot of work/high barrier for entry to using the application.

I created [BookEnricher](src/main/java/noorg/bookparsing/enrich/BookEnricher.java) interface, with an [AbstractBookEnricher](src/main/java/noorg/bookparsing/enrich/AbstractBookEnricher.java) for any shared functionality. I've created the following implementations:


- [BacklogBookEnricher](src/main/java/noorg/bookparsing/enrich/BacklogBookEnricher.java) - Tracks which books you previously owned but hadn't read yet.
- [ContributorGenderEnricher](src/main/java/noorg/bookparsing/enrich/ContributorGenderEnricher.java) - Sets the author's [gender](src/main/java/noorg/bookparsing/domain/types/ContributorGender.java) based on the shelves a book is on.
- [GenreEnricher](src/main/java/noorg/bookparsing/enrich/GenreEnricher.java) - Sets the [genre](src/main/java/noorg/bookparsing/domain/types/BookGenre.java) of a book based on the shelves a book is on.
- [GraphicNovelEnricher](src/main/java/noorg/bookparsing/enrich/GraphicNovelEnricher.java) - Sets the [Format](src/main/java/noorg/bookparsing/domain/types/BookFormat.java) to Graphic Novel based on the shelves a book is on.
- [ReadHistoryEnricher](src/main/java/noorg/bookparsing/enrich/ReadHistoryEnricher.java) - Sets the years a book was read based on the shelves a book is on.

### Shelving Books for Enrichment

This is a list of shelves that can be added to data to take advantage of existing data enrichment code. It is likely not complete.

- author-female - Adding a book to this shelf combination with the [ContributorGenderEnricher](src/main/java/noorg/bookparsing/enrich/ContributorGenderEnricher.java) will set the author's [Gender](src/main/java/noorg/bookparsing/domain/types/ContributorGender.java) to Female
- author-male - Adding a book to this shelf in combination with the [ContributorGenderEnricher](src/main/java/noorg/bookparsing/enrich/ContributorGenderEnricher.java) will set the author's [Gender](src/main/java/noorg/bookparsing/domain/types/ContributorGender.java) to Male
- author-non_binary - Adding a book to this shelf in combination with the [ContributorGenderEnricher](src/main/java/noorg/bookparsing/enrich/ContributorGenderEnricher.java) will set the author's [Gender](src/main/java/noorg/bookparsing/domain/types/ContributorGender.java) to Non-Binary
- genre shelves - Set the book genre if one of the following is present.
  - fantasy
  - historical
  - horror
  - humor
  - mystery
  - nonfiction
  - non-fiction
  - romance
  - steampunk
  - science-fiction
  - scifi
  - sci-fi
  - thriller
- graphic-novel - Adding a book to this shelf in combination with the [GraphicNovelEnricher](src/main/java/noorg/bookparsing/enrich/GraphicNovelEnricher.java) will set the [Format](src/main/java/noorg/bookparsing/domain/types/BookFormat.java) to Graphic Novel
- manga - Adding a book to this shelf in combination with the [GraphicNovelEnricher](src/main/java/noorg/bookparsing/enrich/GraphicNovelEnricher.java) will set the [Format](src/main/java/noorg/bookparsing/domain/types/BookFormat.java) to Graphic Novel
- own-backlog - Adding a book to these shelves in combination with the [BacklogBookEnricher](src/main/java/noorg/bookparsing/enrich/BacklogBookEnricher.java) will track how many books you read for your horde.
- read-YYYY (ex: read-2016) -  Adding a book to these shelves in combination with the [ReadHistoryEnricher](src/main/java/noorg/bookparsing/enrich/ReadHistoryEnricher.java), re-read books can be set to have multiple years beyond what is determined from the read date field.
- read-before-goodreads - the [ReadHistoryEnricher](src/main/java/noorg/bookparsing/enrich/ReadHistoryEnricher.java) also supports tracking books you first read before tracking your data on goodreads. This is helpful when you don't know what year you first read a book.
 

## Reporting

Once your data has been parsed/enriched, you want to do something with it. I've created some basic interfaces, domain objects and services for the purposes of "Reporting". In other words spit out something that I find useful about my books. It was written in such a way to hopefully allow someone else to come along and extend/implement there own Reports and services to spit out things useful to them as well.

### Domain Objects

The core of the reporting are the domain objects that add one or [Book](src/main/java/noorg/bookparsing/domain/Book.java) objects to a [Report](src/main/java/noorg/bookparsing/domain/report/Report.java).

- [AbstractBookCountsYearToYearReport](src/main/java/noorg/bookparsing/domain/report/AbstractBookCountsYearToYearReport.java) - Shared functionality to generate a report from one of the Map<?,Integer> count maps generated by a [YearlyReport](src/main/java/noorg/bookparsing/domain/report/YearlyReport.java).
- [AbstractMultipleYearReport](src/main/java/noorg/bookparsing/domain/report/AbstractMultipleYearReport.java) - Shared functionality for creating a report comparing books across more than one year.
- [AbstractReport](src/main/java/noorg/bookparsing/domain/report/AbstractReport.java) - Shared Report functionality.
- [AbstractYearToYearReport](src/main/java/noorg/bookparsing/domain/report/AbstractYearToYearReport.java) - Shared functionality for creating a tabular report comparing 2 or more [YearlyReports](src/main/java/noorg/bookparsing/domain/report/YearlyReport.java).
- [BookFormatYearToYearReport](src/main/java/noorg/bookparsing/domain/report/BookFormatYearToYearReport.java) - Report comparing the [BookFormats](src/main/java/noorg/bookparsing/domain/types/BookFormat.java) read between 2 or more years.
- [BookGenreYearToYearReport](src/main/java/noorg/bookparsing/domain/report/BookGenreYearToYearReport.java) - Report comparing the [BookGenres](src/main/java/noorg/bookparsing/domain/types/BookGenre.java) read between 2 or more years.
- [BookRatingsYearToYearReport](src/main/java/noorg/bookparsing/domain/report/BookRatingsYearToYearReport.java) - Report comparing the ratings given to books between 2 or more years.
- [DecadeYearToYearReport](src/main/java/noorg/bookparsing/domain/report/DecadeYearToYearReport.java) - Report comparing the year of publication between 2 or more years of reading.
- [GenderReport](src/main/java/noorg/bookparsing/domain/report/GenderReport.java) - Report about a specific [Gender](src/main/java/noorg/bookparsing/domain/types/ContributorGender.java)
- [GenderYearToYearReport](src/main/java/noorg/bookparsing/domain/report/GenderYearToYearReport.java) - Report comparing the [Gender](src/main/java/noorg/bookparsing/domain/types/ContributorGender.java) of the author's of books read between 2 or more years.
- [GenreReport](src/main/java/noorg/bookparsing/domain/report/GenreReport.java) - Report about a specific [Genre](src/main/java/noorg/bookparsing/domain/types/BookGenre.java)
- [ReadingQuantityYearToYearReport](src/main/java/noorg/bookparsing/domain/report/ReadingQuantityYearToYearReport.java) - Report comparing the numer of books read/listened to between 2 or more years.
- [YearlyReport](src/main/java/noorg/bookparsing/domain/report/YearlyReport.java) - Report about a specific year.

### Services

So far there is one service interface: [ReportService](src/main/java/noorg/bookparsing/report/ReportService.java) with an abstract implementation ([AbstractReportService](src/main/java/noorg/bookparsing/report/impl/AbstractReportService.java)) for shared code.

I've created the following concrete implementations so far:

- [AuthorCountsReportService](src/main/java/noorg/bookparsing/report/impl/AuthorCountsReportService.java) - Reports your top N read Authors
- [GenderReportService](src/main/java/noorg/bookparsing/report/impl/GenderReportService.java) - Creates a [GenderReport](src/main/java/noorg/bookparsing/domain/report/GenderReport.java) for each [Gender](src/main/java/noorg/bookparsing/domain/types/ContributorGender.java) you've read and outputs a genre-by-genre breakdown to allow you to evaluate your reading habbits from an author gender perspective.
- [GenreReportService](src/main/java/noorg/bookparsing/report/impl/GenreReportService.java) - Creates a [GenreReport](src/main/java/noorg/bookparsing/domain/report/GenreReport.java) for each [Genre](src/main/java/noorg/bookparsing/domain/types/BookGenre.java) you've read and outputs a genre-by-genre breakdown to allow you to evaluate your reading habbits from a genre perspective.
- [YearlyReportService](src/main/java/noorg/bookparsing/report/impl/YearlyReportService.java) - Create a [YearlyReport](src/main/java/noorg/bookparsing/domain/report/YearlyReport.java) for the year(s) specified and output the results. Additionally all the generated [YearlyReports](src/main/java/noorg/bookparsing/domain/report/YearlyReport.java) will in turn generate several Year-to-Year reports.


This Read Me continues to be a work in progress. Hopefully it gives a decent overview of what's available. It should ideally be updated as new functionality/services/reports are added.
