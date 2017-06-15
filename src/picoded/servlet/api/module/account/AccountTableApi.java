package picoded.servlet.api.module.account;

import picoded.servlet.api.*;
import picoded.servlet.api.module.ApiModule;
import picoded.dstack.module.account.*;
import picoded.dstack.*;

///
/// Account table API builder
///
public class AccountTableApi implements ApiModule {

	/// The AccountTable reference
	protected AccountTable table = null;

	/// Setup the account table api class
	///
	/// @param  The input AccountTable to use
	public AccountTableApi(AccountTable inTable) {
		table = inTable;
	}

	
}