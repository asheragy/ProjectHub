// Run with 'apollo client:download-schema'

module.exports = {
  client: {
    service: {      
		name: 'github',      
		url: 'https://api.github.com/graphql',

		headers: {        
			authorization: 'Bearer xx'
		}      
		// optional disable SSL validation check      skipSSLValidation: true
	}		
  }
}