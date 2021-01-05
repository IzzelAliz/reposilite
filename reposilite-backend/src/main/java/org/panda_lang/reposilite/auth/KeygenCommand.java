/*
 * Copyright (c) 2020 Dzikoysk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.panda_lang.reposilite.auth;

import org.panda_lang.reposilite.console.ReposiliteCommand;
import org.panda_lang.utilities.commons.collection.Pair;
import org.panda_lang.utilities.commons.function.Option;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.util.List;

@Command(name = "keygen", description = "Generate a new access token for the given path")
final class KeygenCommand implements ReposiliteCommand {

    @Parameters(index = "0", paramLabel = "<path>", description = "assigned path")
    private String path;
    @Parameters(index = "1", paramLabel = "<alias>", description = "associated alias")
    private String alias;
    @Parameters(index = "2", paramLabel = "[<permissions>]", description = "extra permissions (w - write, m - manager)", defaultValue = "")
    private String permissions;

    private final TokenService tokenService;

    KeygenCommand(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public boolean execute(List<String> response) {
        String processedPath = path;

        if (path.contains(".") && !path.contains("/")) {
            processedPath = "*/" + path.replace(".", "/");
        }

        Option<Token> previousToken = tokenService.getToken(alias);

        try {
            Pair<String, Token> token = tokenService.createToken(processedPath, alias, permissions);
            response.add("Generated new access token for " + alias + " (" + processedPath + ") with '" + permissions + "' permissions");
            response.add(token.getKey());
            tokenService.saveTokens();
            return true;
        }
        catch (IOException ioException) {
            response.add("Cannot generate token due to: " + ioException.getMessage());

            previousToken.peek(token -> {
                tokenService.addToken(token);
                response.add("The former token has been restored.");
            });

            return false;
        }
    }

}
