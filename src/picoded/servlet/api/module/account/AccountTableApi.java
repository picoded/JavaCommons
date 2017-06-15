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

	public ApiFunction status = (req,res) -> {

		return res;
	};

	/// Does the actual setup for the API
	/// Given the API Builder, and the namespace prefix
	///
	/// @param  API builder to add the required functions
	/// @param  Path to assume
	public void setupApiBuilder(ApiBuilder builder, String subpath) {

	}
}