package dtu.TokenService.Application;

import java.util.List;

import dtu.TokenService.Domain.Entities.Token;
import dtu.TokenService.Domain.Interfaces.ITokenRepository;

public class TokenService {

	private ITokenRepository tokenRepository;



	public TokenService(ITokenRepository tokenRepository) {
		this.tokenRepository = tokenRepository;
	}

	public List<Token> createTokens(Integer numOfTokens, String customerId) {
		List<Token> tokens = tokenRepository.get(customerId);
		if(numOfTokens > 0 && numOfTokens < 6 && tokens.size() < 2) {
			for( int i = 0; i < numOfTokens; i++) {
				tokens.add(tokenRepository.create(customerId));
			}
		}
		return tokens;
	}

	public List<Token> getTokens(String customerId) {
		return tokenRepository.get(customerId);
	}

	public boolean deleteTokensForCustomer(String customerId) {
		return tokenRepository.delete(customerId);
	}
	
	public Token getVerifiedToken(String tokenUuid) {
		return tokenRepository.getVerfiedToken(tokenUuid);
	}

//	public Boolean verifyToken(String tokenUuid) {
//		return tokenRepository.verifyToken(tokenUuid);
//	}
}