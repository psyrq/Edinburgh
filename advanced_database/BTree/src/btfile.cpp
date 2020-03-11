#include "minirel.h"
#include "bufmgr.h"
#include "db.h"
#include "new_error.h"
#include "btfile.h"
#include "btfilescan.h"


//-------------------------------------------------------------------
// BTreeFile::BTreeFile
//
// Input   : filename - filename of an index.  
// Output  : returnStatus - status of execution of constructor. 
//           OK if successful, FAIL otherwise.
// Purpose : If the B+ tree exists, open it.  Otherwise create a
//           new B+ tree index.
//-------------------------------------------------------------------

BTreeFile::BTreeFile (Status& returnStatus, const char* filename) 
{
	// TODO: add your code here
	Status getEntry;

	getEntry = MINIBASE_DB->GetFileEntry();
	switch(getEntry) {
		case OK:
			Page *page;
			MINIBASE_DB->GetFileEntry(filename);
			returnStatus = OK;
		case FAIL:
			MINIBASE_BM->NewPage(pid, page);
			MINIBASE_DB->AddFileEntry(filename, pid);
			((SortedPage*)page)->Init(pid);
			returnStatus = FAIL;
	}
	
}


//-------------------------------------------------------------------
// BTreeFile::~BTreeFile
//
// Input   : None 
// Output  : None
// Purpose : Clean Up
//-------------------------------------------------------------------

BTreeFile::~BTreeFile()
{
	// TODO: add your code here
}


//-------------------------------------------------------------------
// BTreeFile::DestroyFile
//
// Input   : None
// Output  : None
// Return  : OK if successful, FAIL otherwise.
// Purpose : Delete the entire index file by freeing all pages allocated
//           for this BTreeFile.
//-------------------------------------------------------------------

Status 
BTreeFile::DestroyFile()
{
	// TODO: add your code here
	return OK;
}


//-------------------------------------------------------------------
// BTreeFile::Insert
//
// Input   : key - the value of the key to be inserted.
//           rid - RecordID of the record to be inserted.
// Output  : None
// Return  : OK if successful, FAIL otherwise.
// Purpose : Insert an index entry with this rid and key.
// Note    : If the root didn't exist, create it.
//-------------------------------------------------------------------


Status 
BTreeFile::Insert(const int key, const RecordID rid)
{
	// TODO: add your code here
	return OK;
}


//-------------------------------------------------------------------
// BTreeFile::Delete
//
// Input   : key - the value of the key to be deleted.
//           rid - RecordID of the record to be deleted.
// Output  : None
// Return  : OK if successful, FAIL otherwise. 
// Purpose : Delete an index entry with this rid and key.  
// Note    : If the root becomes empty, delete it.
//-------------------------------------------------------------------

Status 
BTreeFile::Delete(const int key, const RecordID rid)
{
	// TODO: add your code here
	return OK;
}


//-------------------------------------------------------------------
// BTreeFile::OpenScan
//
// Input   : lowKey, highKey - pointer to keys, indicate the range
//                             to scan.
// Output  : None
// Return  : A pointer to IndexFileScan class.
// Purpose : Initialize a scan.  
// Note    : Usage of lowKey and highKey :
//
//           lowKey      highKey      range
//           value       value  
//           --------------------------------------------------
//           nullptr     nullptr      whole index
//           nullptr     !nullptr     minimum to highKey
//           !nullptr    nullptr      lowKey to maximum
//           !nullptr    =lowKey      exact match
//           !nullptr    >lowKey      lowKey to highKey
//-------------------------------------------------------------------

IndexFileScan*
BTreeFile::OpenScan(const int* lowKey, const int* highKey)
{
	// TODO: add your code here
	return nullptr;
}


//-------------------------------------------------------------------
// BTreeFile::PrintTree
//
// Input   : pageID - root of the tree to print.
// Output  : None
// Return  : None
// Purpose : Print out the content of the tree rooted at pid.
//-------------------------------------------------------------------

Status 
BTreeFile::PrintTree(PageID pageID)
{ 
	if ( pageID == INVALID_PAGE ) {
		return FAIL;
	}

	SortedPage* page = nullptr;
	PIN(pageID, page);

	NodeType type = (NodeType) page->GetType();
	if (type == INDEX_NODE)
	{
		BTIndexPage* index = (BTIndexPage *) page;
		PageID curPageID = index->GetLeftLink();
		PrintTree(curPageID);

		RecordID curRid;
		int key;        
		Status s = index->GetFirst(key, curPageID, curRid);
		while (s != DONE)
		{
			PrintTree(curPageID);
			s = index->GetNext(key, curPageID, curRid);
		}
	}

	UNPIN(pageID, CLEAN);
	PrintNode(pageID);

	return OK;
}

//-------------------------------------------------------------------
// BTreeFile::PrintNode
//
// Input   : pageID - the node to print.
// Output  : None
// Return  : None
// Purpose : Print out the content of the node pid.
//-------------------------------------------------------------------

Status 
BTreeFile::PrintNode(PageID pageID)
{   
	SortedPage* page = nullptr;
	PIN(pageID, page);

	NodeType type = (NodeType) page->GetType();
	switch (type)
	{
		case INDEX_NODE:
		{
			BTIndexPage* index = (BTIndexPage *) page;
			PageID curPageID = index->GetLeftLink();
			cout << "\n---------------- Content of index node " << pageID << "-----------------------------" << endl;
			cout << "\n Left most PageID:  "  << curPageID << endl;

			RecordID currRid;
			int key, i = 0;

			Status s = index->GetFirst(key, curPageID, currRid); 
			while (s != DONE)
			{   
				i++;
				cout <<  "Key: " << key << "    PageID: " << curPageID << endl;
				s = index->GetNext(key, curPageID, currRid);
			}
			cout << "\n This page contains  " << i << "  entries." << endl;
			break;
		}

		case LEAF_NODE:
		{
			BTLeafPage* leaf = (BTLeafPage *) page;
			cout << "\n---------------- Content of leaf node " << pageID << "-----------------------------" << endl;

			RecordID dataRid, currRid;
			int key, i = 0;

			Status s = leaf->GetFirst(key, dataRid, currRid);
			while (s != DONE)
			{   
				i++;
				cout << "DataRecord ID: " << dataRid << " Key: " << key << endl;
				s = leaf->GetNext(key, dataRid, currRid);
			}
			cout << "\n This page contains  " << i << "  entries." << endl;
			break;
		}
	}
	UNPIN(pageID, CLEAN);

	return OK;
}

//-------------------------------------------------------------------
// BTreeFile::Print
//
// Input   : None
// Output  : None
// Return  : None
// Purpose : Print out this B+ Tree
//-------------------------------------------------------------------

Status 
BTreeFile::Print()
{   
	cout << "\n\n-------------- Now Begin Printing a new whole B+ Tree -----------" << endl;

	if (PrintTree(rootPid) == OK)
		return OK;

	return FAIL;
}

//-------------------------------------------------------------------
// BTreeFile::DumpStatistics
//
// Input   : None
// Output  : None
// Return  : None
// Purpose : Print out the following statistics.
//           1. Total number of leaf nodes, and index nodes.
//           2. Total number of leaf entries.
//           3. Total number of index entries.
//           4. Mean, Min, and max fill factor of leaf nodes and 
//              index nodes.
//           5. Height of the tree.
//-------------------------------------------------------------------
Status 
BTreeFile::DumpStatistics()
{   
	// TODO: add your code here 
	return OK;
}
